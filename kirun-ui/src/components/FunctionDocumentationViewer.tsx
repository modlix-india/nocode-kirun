import React, { useState, useMemo, useEffect } from 'react';
import {
	initializeDocumentation,
	getAllFunctionDocumentation,
	getFunctionsByTopLevelNamespace,
	searchFunctionDocumentation,
	type FunctionDocumentation,
} from '../FunctionDocumentationRegistry';
import FunctionDetailModal from './FunctionDetailModal';
import '../css/FunctionDocumentationViewer.css';

export default function FunctionDocumentationViewer() {
	const [searchTerm, setSearchTerm] = useState('');
	const [selectedNamespace, setSelectedNamespace] = useState<string>('all');
	const [selectedFunction, setSelectedFunction] = useState<FunctionDocumentation | null>(null);
	const [platformFilter, setPlatformFilter] = useState<'all' | 'JS' | 'Java'>('all');
	const [isLoading, setIsLoading] = useState(true);

	// Initialize documentation on mount (lazy load)
	useEffect(() => {
		initializeDocumentation()
			.then(() => setIsLoading(false))
			.catch(err => {
				console.error('Failed to load documentation:', err);
				setIsLoading(false);
			});
	}, []);

	// Get all functions grouped by namespace
	const functionsByNamespace = useMemo(() => getFunctionsByTopLevelNamespace(), []);

	// Get filtered functions based on search and filters
	const filteredFunctions = useMemo(() => {
		let functions: FunctionDocumentation[] = [];

		if (searchTerm.trim()) {
			functions = searchFunctionDocumentation(searchTerm);
		} else if (selectedNamespace === 'all') {
			functions = getAllFunctionDocumentation();
		} else {
			functions = functionsByNamespace[selectedNamespace] || [];
		}

		// Apply platform filter
		if (platformFilter !== 'all') {
			functions = functions.filter(fn => fn.availableIn.includes(platformFilter));
		}

		return functions.sort((a, b) => a.fullName.localeCompare(b.fullName));
	}, [searchTerm, selectedNamespace, platformFilter, functionsByNamespace]);

	// Get all unique top-level namespaces
	const namespaces = useMemo(() => Object.keys(functionsByNamespace).sort(), [functionsByNamespace]);

	const handleFunctionClick = (fn: FunctionDocumentation) => {
		setSelectedFunction(fn);
	};

	const closeModal = () => {
		setSelectedFunction(null);
	};

	// Show loading state
	if (isLoading) {
		return (
			<div className="_function-documentation-viewer">
				<div className="_function-doc-header">
					<h1>KIRun Function Documentation</h1>
					<p className="_function-doc-subtitle">Loading documentation...</p>
				</div>
			</div>
		);
	}

	return (
		<div className="_function-documentation-viewer">
			{/* Header */}
			<div className="_function-doc-header">
				<h1>KIRun Function Documentation</h1>
				<p className="_function-doc-subtitle">
					Browse and search {getAllFunctionDocumentation().length} functions across System,
					Math, String, Array, Date, Object, and more
				</p>
			</div>

			{/* Search and Filters */}
			<div className="_function-doc-controls">
				<input
					type="text"
					className="_function-doc-search"
					placeholder="Search functions..."
					value={searchTerm}
					onChange={e => setSearchTerm(e.target.value)}
				/>

				<select
					className="_function-doc-namespace-filter"
					value={selectedNamespace}
					onChange={e => setSelectedNamespace(e.target.value)}
					disabled={!!searchTerm}
				>
					<option value="all">All Namespaces</option>
					{namespaces.map(ns => (
						<option key={ns} value={ns}>
							{ns}
						</option>
					))}
				</select>

				<select
					className="_function-doc-platform-filter"
					value={platformFilter}
					onChange={e => setPlatformFilter(e.target.value as 'all' | 'JS' | 'Java')}
				>
					<option value="all">All Platforms</option>
					<option value="JS">JavaScript Only</option>
					<option value="Java">Java Only</option>
				</select>
			</div>

			{/* Results Info */}
			<div className="_function-doc-results-info">
				{searchTerm && (
					<p>
						Found <strong>{filteredFunctions.length}</strong> function
						{filteredFunctions.length !== 1 ? 's' : ''} matching "{searchTerm}"
					</p>
				)}
				{!searchTerm && (
					<p>
						Showing <strong>{filteredFunctions.length}</strong> function
						{filteredFunctions.length !== 1 ? 's' : ''}
						{selectedNamespace !== 'all' && ` in ${selectedNamespace}`}
					</p>
				)}
			</div>

			{/* Function List */}
			<div className="_function-doc-list">
				{filteredFunctions.length === 0 ? (
					<div className="_function-doc-empty">
						<p>No functions found matching your criteria.</p>
					</div>
				) : (
					filteredFunctions.map(fn => (
						<div
							key={fn.fullName}
							className="_function-doc-item"
							onClick={() => handleFunctionClick(fn)}
						>
							<div className="_function-doc-item-header">
								<h3 className="_function-doc-item-name">{fn.fullName}</h3>
								<div className="_function-doc-item-badges">
									{fn.availableIn.map(platform => (
										<span key={platform} className={`_function-doc-badge _${platform.toLowerCase()}`}>
											{platform}
										</span>
									))}
								</div>
							</div>
							<p className="_function-doc-item-description">{fn.description}</p>
						</div>
					))
				)}
			</div>

			{/* Detail Modal */}
			{selectedFunction && (
				<FunctionDetailModal functionDoc={selectedFunction} onClose={closeModal} />
			)}
		</div>
	);
}
