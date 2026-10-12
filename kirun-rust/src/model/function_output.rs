//! Port of kirun-js `model/FunctionOutput.ts` and `model/FunctionOutputGenerator.ts`.

use crate::model::EventResult;

/// JS `FunctionOutputGenerator`: lazily yields events (loops yield one `iteration` per call).
pub trait FunctionOutputGenerator: Send + Sync {
    fn next(&mut self) -> Option<EventResult>;
}

impl<F: FnMut() -> Option<EventResult> + Send + Sync> FunctionOutputGenerator for F {
    fn next(&mut self) -> Option<EventResult> {
        self()
    }
}

pub struct FunctionOutput {
    fo: Vec<EventResult>,
    index: usize,
    generator: Option<Box<dyn FunctionOutputGenerator>>,
}

impl FunctionOutput {
    pub fn new(results: Vec<EventResult>) -> Self {
        Self { fo: results, index: 0, generator: None }
    }

    pub fn from_generator(generator: impl FunctionOutputGenerator + 'static) -> Self {
        Self { fo: Vec::new(), index: 0, generator: Some(Box::new(generator)) }
    }

    pub fn next(&mut self) -> Option<EventResult> {
        match self.generator.as_mut() {
            None => {
                let er = self.fo.get(self.index).cloned();
                if er.is_some() {
                    self.index += 1;
                }
                er
            }
            Some(generator) => {
                let er = generator.next();
                if let Some(er) = &er {
                    self.fo.push(er.clone());
                }
                er
            }
        }
    }

    /// Results produced so far (all of them for a non-generator output).
    pub fn all_results(&self) -> &Vec<EventResult> {
        &self.fo
    }
}

impl std::fmt::Debug for FunctionOutput {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        f.debug_struct("FunctionOutput")
            .field("fo", &self.fo)
            .field("index", &self.index)
            .field("generator", &self.generator.is_some())
            .finish()
    }
}
