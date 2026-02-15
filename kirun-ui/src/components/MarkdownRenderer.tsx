import React, { useMemo } from 'react';
import '../css/MarkdownRenderer.css';

interface MarkdownRendererProps {
	content: string;
}

/**
 * Simple Markdown Renderer
 * Supports basic markdown syntax: headers, lists, code blocks, inline code, bold, italic, links
 *
 * For more advanced markdown rendering, consider installing react-markdown:
 * npm install react-markdown
 */
export default function MarkdownRenderer({ content }: MarkdownRendererProps) {
	const html = useMemo(() => {
		if (!content) return '';

		let result = content;

		// Escape HTML
		result = result.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');

		// Code blocks (```...```)
		result = result.replace(/```([^\n]*)\n([\s\S]*?)```/g, (_, lang, code) => {
			return `<pre class="_markdown-code-block"><code class="language-${lang}">${code.trim()}</code></pre>`;
		});

		// Inline code (`...`)
		result = result.replace(/`([^`]+)`/g, '<code class="_markdown-inline-code">$1</code>');

		// Headers (must be processed before bold to avoid ** conflicts)
		result = result.replace(/^###\s+(.*?)$/gim, '<h3>$1</h3>');
		result = result.replace(/^##\s+(.*?)$/gim, '<h2>$1</h2>');
		result = result.replace(/^#\s+(.*?)$/gim, '<h1>$1</h1>');

		// Bold (**text**) - process after headers to avoid ## confusion
		result = result.replace(/\*\*([^\*\n]+)\*\*/g, '<strong>$1</strong>');

		// Italic (*text* or _text_)
		result = result.replace(/\*([^\*]+)\*/g, '<em>$1</em>');
		result = result.replace(/_([^_]+)_/g, '<em>$1</em>');

		// Links [text](url)
		result = result.replace(/\[([^\]]+)\]\(([^\)]+)\)/g, '<a href="$2" target="_blank" rel="noopener noreferrer">$1</a>');

		// Unordered lists - convert list items first
		result = result.replace(/^\* (.*$)/gim, '<li>$1</li>');
		result = result.replace(/^- (.*$)/gim, '<li>$1</li>');

		// Ordered lists
		result = result.replace(/^\d+\. (.*$)/gim, '<li>$1</li>');

		// Wrap consecutive <li> tags in <ul> (non-greedy, line by line)
		result = result.replace(/(<li>.*?<\/li>\n?)+/g, (match) => `<ul>${match}</ul>`);

		// Line breaks (double newline = paragraph)
		result = result.replace(/\n\n/g, '</p><p>');
		result = '<p>' + result + '</p>';

		// Clean up empty paragraphs
		result = result.replace(/<p><\/p>/g, '');
		result = result.replace(/<p>(<h[1-6]>)/g, '$1');
		result = result.replace(/(<\/h[1-6]>)<\/p>/g, '$1');
		result = result.replace(/<p>(<pre)/g, '$1');
		result = result.replace(/(<\/pre>)<\/p>/g, '$1');
		result = result.replace(/<p>(<ul)/g, '$1');
		result = result.replace(/(<\/ul>)<\/p>/g, '$1');

		return result;
	}, [content]);

	return (
		<div
			className="_markdown-renderer"
			dangerouslySetInnerHTML={{ __html: html }}
		/>
	);
}
