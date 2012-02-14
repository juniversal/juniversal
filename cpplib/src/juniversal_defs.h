/**
 * This file contains the basic types and other definitions used by JUniversal.
 */
#ifndef JUNIVERSAL_DEFS_H
#define JUNIVERSAL_DEFS_H

#include <stddef.h>
#include <stdlib.h>

#define JU_USING_STD_NAMESPACES  using namespace juniversal;  using namespace java_lang;


namespace juniversal {

typedef unsigned short unichar;
typedef unsigned long ReferenceCount;

#if defined(_MSC_VER)
typedef signed __int64 long64;
typedef unsigned __int64 ulong64;
#else
typedef signed long long long64;
typedef unsigned long long ulong64;
#endif


inline char rightShiftUnsigned(char value, int bits) {
	return reinterpret_cast<char>( reinterpret_cast<unsigned char>(value) >> bits );
}

inline short rightShiftUnsigned(short value, int bits) {
	return reinterpret_cast<short>( reinterpret_cast<unsigned short>(value) >> bits );
}

inline int rightShiftUnsigned(int value, int bits) {
	return reinterpret_cast<int>( reinterpret_cast<unsigned int>(value) >> bits );
}

inline long64 rightShiftUnsigned(long64 value, int bits) {
	return reinterpret_cast<long64>( reinterpret_cast<ulong64>(value) >> bits );
}

/**
 * Smart pointer type.
 */
template <class T> class ptr {
public:
	ptr<T>() { m_object = NULL; }
	~ptr<T>() {  }
	//operator T* { return m_object; }

private: // Data
	T* m_object;
};


}  // namespace juniversal
#endif // JUNIVERSAL_DEFS_H
