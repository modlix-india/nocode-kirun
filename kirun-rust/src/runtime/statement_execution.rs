//! Port of kirun-js `runtime/StatementExecution.ts`.

use indexmap::IndexSet;

use crate::model::Statement;
use crate::runtime::statement_message::{StatementMessage, StatementMessageType};

#[derive(Debug, Clone)]
pub struct StatementExecution {
    pub statement: Statement,
    pub messages: Vec<StatementMessage>,
    pub dependencies: IndexSet<String>,
}

impl PartialEq for StatementExecution {
    /// JS equality compares statement names only.
    fn eq(&self, other: &Self) -> bool {
        self.statement.statement_name == other.statement.statement_name
    }
}

impl StatementExecution {
    pub fn new(statement: Statement) -> Self {
        Self { statement, messages: Vec::new(), dependencies: IndexSet::new() }
    }
    pub fn get_statement(&self) -> &Statement {
        &self.statement
    }
    pub fn get_messages(&self) -> &Vec<StatementMessage> {
        &self.messages
    }
    pub fn get_dependencies(&self) -> &IndexSet<String> {
        &self.dependencies
    }
    pub fn get_unique_key(&self) -> &str {
        &self.statement.statement_name
    }
    pub fn add_message(&mut self, message_type: StatementMessageType, message: impl Into<String>) {
        self.messages.push(StatementMessage::new(Some(message_type), Some(message.into())));
    }
    pub fn add_dependency(&mut self, dependency: impl Into<String>) {
        self.dependencies.insert(dependency.into());
    }
}
