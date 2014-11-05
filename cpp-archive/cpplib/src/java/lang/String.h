#ifndef STRING_H
#define STRING_H

#include "juniversal_defs.h"
#include "java/lang/Object.h"


namespace java_lang {

JU_USING_STD_NAMESPACES

class String : public Object {
public:
	String();

	unichar charAt(int index) {
        if (0 <= index && index < m_length)
            return m_data[index];
        throw new StringIndexOutOfBoundsException();
	}

	int length() { return m_length; }

	virtual ~String();

private: // Data
	int m_length;
	unichar m_data[0];
};

} // java_lang
#endif // STRING_H
