#ifndef ARRAY_H
#define ARRAY_H

#include "juniversal_defs.h"
#include "java/lang/Object.h"


namespace juniversal {

JU_USING_STD_NAMESPACES

/**
 * Array type, corresponding to Java arrays.
 */
template <typename T> class Array : public Object {
public:
	static void * operator new(size_t objectSize, unsigned int arrayLength) {
		void* p = Object::operator new(objectSize + sizeof(T) * arrayLength);

		// Set the length so it doesn't have to also be passed to the constructor
		reinterpret_cast<Array*>(p)->m_length = arrayLength;

		return p;
	}

	static void operator delete (void *p) {
		Object::operator delete(p);
	}

	unsigned int length() { return m_length; }

private: // Data
	unsigned int m_length;
	T m_data[0];
};

} // java_lang
#endif // STRING_H
