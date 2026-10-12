//! Port of kirun-js `__tests__/engine/util/LinkedListTest.ts`.

use kirun::util::linked_list::LinkedList;

#[test]
fn linked_list_test() {
    let mut x: LinkedList<i64> = LinkedList::new();
    x.push(10);
    x.push(20);
    assert!(!x.is_empty());
    assert_eq!(x.size(), 2);
    assert_eq!(x.pop().unwrap(), 20);
    assert!(!x.is_empty());
    assert_eq!(x.pop().unwrap(), 10);
    assert!(x.is_empty());

    x = LinkedList::new();
    x.push(230);
    x.push(231);
    x.push(233);

    assert_eq!(x.to_array(), vec![233, 231, 230]);

    x = LinkedList::from_vec(vec![5, 6, 7]);
    assert_eq!(x.to_array(), vec![5, 6, 7]);

    x = LinkedList::new();
    x.add_all(vec![1, 2, 3]);
    assert_eq!(x.to_array(), vec![1, 2, 3]);
    x.add(4);
    assert_eq!(x.to_array(), vec![1, 2, 3, 4]);
}

#[test]
fn linked_list_test_set_at_every_index() {
    // the walk in set() used to re-read from head each iteration, so any index
    // of 2 or more wrote over element 1 instead of the requested one
    for i in 0..5usize {
        let mut x = LinkedList::from_vec(vec![10, 20, 30, 40, 50]);
        x.set(i as i64, 999).unwrap();
        let mut expected = vec![10, 20, 30, 40, 50];
        expected[i] = 999;
        assert_eq!(x.to_array(), expected);
    }
    let mut y = LinkedList::from_vec(vec![1, 2, 3]);
    assert!(y.set(-1, 0).is_err());
    assert!(y.set(3, 0).is_err());
}

#[test]
fn linked_list_test_peek_last_test() {
    let mut x = LinkedList::new();
    x.push(230);
    x.push(231);
    x.push(233);

    assert_eq!(x.peek_last().unwrap(), 230);
}
