//! Port of kirun-js `util/LinkedList.ts`: a doubly linked list used as a stack (push/pop at
//! the head) and a queue (add at the tail). Backed by a `VecDeque` (front = JS head).

use std::collections::VecDeque;

use crate::{KIRunError, Result};

#[derive(Debug, Clone, PartialEq, Default)]
pub struct LinkedList<T> {
    items: VecDeque<T>,
}

impl<T: Clone> LinkedList<T> {
    pub fn new() -> Self {
        Self { items: VecDeque::new() }
    }

    /// JS `new LinkedList(list)`.
    pub fn from_vec(list: Vec<T>) -> Self {
        Self { items: list.into() }
    }

    /// Adds at the head.
    pub fn push(&mut self, value: T) {
        self.items.push_front(value);
    }

    /// Removes from the head; fails when empty.
    pub fn pop(&mut self) -> Result<T> {
        self.items
            .pop_front()
            .ok_or_else(|| KIRunError::runtime("List is empty and cannot pop further."))
    }

    pub fn is_empty(&self) -> bool {
        self.items.is_empty()
    }

    pub fn size(&self) -> usize {
        self.items.len()
    }

    pub fn get(&self, index: i64) -> Result<T> {
        if index < 0 || index as usize >= self.items.len() {
            return Err(KIRunError::runtime(format!("{index} is out of bounds [0,{}]", self.items.len())));
        }
        Ok(self.items[index as usize].clone())
    }

    /// Fails with `Index $ out of bound to set the value in linked list.` when out of range.
    pub fn set(&mut self, index: i64, value: T) -> Result<&mut Self> {
        if index < 0 || index as usize >= self.items.len() {
            return Err(KIRunError::runtime(format!(
                "Index {index} out of bound to set the value in linked list."
            )));
        }
        self.items[index as usize] = value;
        Ok(self)
    }

    pub fn to_array(&self) -> Vec<T> {
        self.items.iter().cloned().collect()
    }

    pub fn peek(&self) -> Result<T> {
        self.items.front().cloned().ok_or_else(|| KIRunError::runtime("List is empty so cannot peak"))
    }

    pub fn peek_last(&self) -> Result<T> {
        self.items.back().cloned().ok_or_else(|| KIRunError::runtime("List is empty so cannot peak"))
    }

    pub fn get_first(&self) -> Result<T> {
        self.items.front().cloned().ok_or_else(|| KIRunError::runtime("List is empty so cannot get first"))
    }

    pub fn remove_first(&mut self) -> Result<T> {
        self.pop()
    }

    pub fn remove_last(&mut self) -> Result<T> {
        self.items.pop_back().ok_or_else(|| KIRunError::runtime("List is empty so cannot remove"))
    }

    pub fn add_all(&mut self, list: Vec<T>) -> &mut Self {
        self.items.extend(list);
        self
    }

    /// Adds at the tail.
    pub fn add(&mut self, t: T) -> &mut Self {
        self.items.push_back(t);
        self
    }
}
