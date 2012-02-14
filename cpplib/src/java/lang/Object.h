#ifndef OBJECT_H
#define OBJECT_H

#include "juniversal_defs.h"


namespace java_lang {

JU_USING_STD_NAMESPACES

class Object {
public:
	Object() {}
	virtual ~Object() {}

	static void* operator new(size_t size) {
		void* pMemory = malloc(sizeof(ReferenceCount) + size);
		*reinterpret_cast<ReferenceCount>(pMemory) = 0;
		return pMemory + sizeof(ReferenceCount);
	}

	static void operator delete (void* pMemory) {
		free(pMemory);
	}

	/*
	virtual ptr<String> toString() {
		// TODO: Implement this
		// Java default: getClass().getName() + '@' + Integer.toHexString(hashCode())
		return NULL;
	}
	*/

	/*
	 * Use an object's address as its default hash code.  Since JUniversal objects don't move
	 * around in memory (as of now), that works well.
	 */
	virtual int hashCode() {
		return reinterpret_cast<int>(this);
	}
};

} // java_lang
#endif // OBJECT_H
