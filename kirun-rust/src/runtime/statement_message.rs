//! Port of kirun-js `runtime/StatementMessage.ts` and `runtime/StatementMessageType.ts`.

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum StatementMessageType {
    Error,
    Warning,
    Message,
}

impl StatementMessageType {
    pub fn name(&self) -> &'static str {
        match self {
            StatementMessageType::Error => "ERROR",
            StatementMessageType::Warning => "WARNING",
            StatementMessageType::Message => "MESSAGE",
        }
    }
}

#[derive(Debug, Clone, PartialEq)]
pub struct StatementMessage {
    pub message_type: Option<StatementMessageType>,
    pub message: Option<String>,
}

impl StatementMessage {
    pub fn new(message_type: Option<StatementMessageType>, message: Option<String>) -> Self {
        Self { message_type, message }
    }
    pub fn get_message_type(&self) -> Option<StatementMessageType> {
        self.message_type
    }
    pub fn get_message(&self) -> Option<&str> {
        self.message.as_deref()
    }
}

impl std::fmt::Display for StatementMessage {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(
            f,
            "{} : {}",
            self.message_type.map(|t| t.name()).unwrap_or("undefined"),
            self.message.as_deref().unwrap_or("undefined")
        )
    }
}
