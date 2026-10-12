//! Port of kirun-js `HybridRepository.ts`: first repository that finds the item wins.

use std::sync::Arc;

use async_trait::async_trait;

use crate::repository::Repository;

pub struct HybridRepository<T: ?Sized + Send + Sync + 'static> {
    repos: Vec<Arc<dyn Repository<T>>>,
}

impl<T: ?Sized + Send + Sync + 'static> HybridRepository<T> {
    pub fn new(repos: Vec<Arc<dyn Repository<T>>>) -> Self {
        Self { repos }
    }
}

#[async_trait]
impl<T: ?Sized + Send + Sync + 'static> Repository<T> for HybridRepository<T> {
    async fn find(&self, namespace: &str, name: &str) -> Option<Arc<T>> {
        for repo in &self.repos {
            if let Some(found) = repo.find(namespace, name).await {
                return Some(found);
            }
        }
        None
    }

    async fn filter(&self, name: &str) -> Vec<String> {
        let mut seen = indexmap::IndexSet::new();
        for repo in &self.repos {
            for n in repo.filter(name).await {
                seen.insert(n);
            }
        }
        seen.into_iter().collect()
    }
}
