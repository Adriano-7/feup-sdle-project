package org.project.data_structures;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A helper class to represent an element and its associated tags in the OR-Set.
 * @param <E> The type of the element
 * @param <T> The type of the tag
 */
class Element<E, T> {
    private final E element;
    private final Set<T> tags;

    public Element(E element) {
        this.element = element;
        this.tags = new HashSet<>();
    }

    public E getElement() {
        return element;
    }

    public Set<T> getTags() {
        return tags;
    }

    public void addTag(T tag) {
        tags.add(tag);
    }

    public void addTags(Collection<T> newTags) {
        tags.addAll(newTags);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Element)) return false;
        Element<?, ?> that = (Element<?, ?>) o;
        return Objects.equals(element, that.element) &&
                Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(element, tags);
    }
}
