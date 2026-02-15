import React from 'react';
import type { FunctionDocumentation } from '../FunctionDocumentationRegistry';
import MarkdownRenderer from './MarkdownRenderer';
import '../css/FunctionDetailModal.css';

interface FunctionDetailModalProps {
	functionDoc: FunctionDocumentation;
	onClose: () => void;
}

export default function FunctionDetailModal({ functionDoc, onClose }: FunctionDetailModalProps) {
	const handleBackdropClick = (e: React.MouseEvent) => {
		if (e.target === e.currentTarget) {
			onClose();
		}
	};

	return (
		<div className="_function-detail-modal-backdrop" onClick={handleBackdropClick}>
			<div className="_function-detail-modal-content">
				{/* Header */}
				<div className="_function-detail-header">
					<h1>{functionDoc.fullName}</h1>
					<button className="_function-detail-close" onClick={onClose} title="Close">
						×
					</button>
				</div>

				{/* Description */}
				<div className="_function-detail-description">
					<p>{functionDoc.description}</p>
				</div>

				{/* Documentation (Markdown) */}
				<div className="_function-detail-documentation">
					<MarkdownRenderer content={functionDoc.documentation} />
				</div>

				{/* Examples */}
				{functionDoc.metadata?.examples && functionDoc.metadata.examples.length > 0 && (
					<div className="_function-detail-examples">
						<h2>Examples</h2>
						{functionDoc.metadata.examples.map((example, index) => (
							<div key={index} className="_function-detail-example">
								<h3>{example.title}</h3>
								<p>{example.description}</p>
								<pre className="_function-detail-example-code">
									<code>{example.code}</code>
								</pre>
							</div>
						))}
					</div>
				)}

				{/* Footer */}
				<div className="_function-detail-footer">
					<button className="_function-detail-close-button" onClick={onClose}>
						Close
					</button>
				</div>
			</div>
		</div>
	);
}
