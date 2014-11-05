/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package ju.util;


/**
 * This class is an abstract implementation of the {@code Map} interface. This
 * implementation does not support adding. A subclass must implement the
 * abstract method entrySet().
 * 
 * @since 1.2
 */
public abstract class AbstractMap<K, V> implements Map<K, V> {

    /**
     * Constructs a new instance of this {@code AbstractMap}.
     */
    protected AbstractMap() {
    }

    /**
     * Compares the specified object to this instance, and returns {@code true}
     * if the specified object is a map and both maps contain the same mappings.
     * 
     * @param object
     *            the object to compare with this object.
     * @return boolean {@code true} if the object is the same as this object,
     *         and {@code false} if it is different from this object.
     * @see #hashCode()
     * @see #entrySet()
     */
    /*
     * TODO: Come back to this
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) object;
            if (size() != map.size()) {
                return false;
            }

            try {
                for (Entry<K, V> entry : entrySet()) {
                    K key = entry.getKey();
                    V mine = entry.getValue();
                    Object theirs = map.get(key);
                    if (mine == null) {
                        if (theirs != null || !map.containsKey(key)) {
                            return false;
                        }
                    } else if (!mine.equals(theirs)) {
                        return false;
                    }
                }
            } catch (NullPointerException ignored) {
                return false;
            } catch (ClassCastException ignored) {
                return false;
            }
            return true;
        }
        return false;
    }
    */

    /**
     * Returns the hash code for this object. Objects which are equal must
     * return the same value for this method.
     * 
     * @return the hash code of this object.
     * @see #equals(Object)
     */
    /*
     * TODO: Come back to this
    @Override
    public int hashCode() {
        int result = 0;
        Iterator<Map.Entry<K, V>> it = entrySet().iterator();
        while (it.hasNext()) {
            result += it.next().hashCode();
        }
        return result;
    }
    */

    /**
     * Returns whether this map is empty.
     * 
     * @return {@code true} if this map has no elements, {@code false}
     *         otherwise.
     * @see #size()
     */
    public boolean isEmpty() {
        return size() == 0;
    }

	/**
	 * Maps the specified key to the specified value.
	 * 
	 * @param key
	 *            the key.
	 * @param value
	 *            the value.
	 * @return the value of any previous mapping with the specified key or
	 *         {@code null} if there was no mapping.
	 * @throws UnsupportedOperationException
	 *             if adding to this map is not supported.
	 * @throws ClassCastException
	 *             if the class of the key or value is inappropriate for this
	 *             map.
	 * @throws IllegalArgumentException
	 *             if the key or value cannot be added to this map.
	 * @throws NullPointerException
	 *             if the key or value is {@code null} and this Map does not
	 *             support {@code null} keys or values.
	 */
	public V put(K key, V value) {
		throw new UnsupportedOperationException();
	}

    /**
     * Returns the string representation of this map.
     * 
     * @return the string representation of this map.
     */
    /*
     * TODO: Come back to this
    @Override
    public String toString() {
        if (isEmpty()) {
            return "{}"; //$NON-NLS-1$
        }

        StringBuilder buffer = new StringBuilder(size() * 28);
        buffer.append('{');
        Iterator<Map.Entry<K, V>> it = entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<K, V> entry = it.next();
            Object key = entry.getKey();
            if (key != this) {
                buffer.append(key);
            } else {
                buffer.append("(this Map)"); //$NON-NLS-1$
            }
            buffer.append('=');
            Object value = entry.getValue();
            if (value != this) {
                buffer.append(value);
            } else {
                buffer.append("(this Map)"); //$NON-NLS-1$
            }
            if (it.hasNext()) {
                buffer.append(", "); //$NON-NLS-1$
            }
        }
        buffer.append('}');
        return buffer.toString();
    }
    */
}
