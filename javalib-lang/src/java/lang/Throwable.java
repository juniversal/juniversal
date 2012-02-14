/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.lang;
import java.lang.IllegalArgumentException;
import java.lang.IllegalStateException;


/**
 * The superclass of all classes which can be thrown by the virtual machine. The
 * two direct subclasses are recoverable exceptions ({@code Exception}) and
 * unrecoverable errors ({@code Error}). This class provides common methods for
 * accessing a string message which provides extra information about the
 * circumstances in which the {@code Throwable} was created (basically an error
 * message in most cases), and for saving a stack trace (that is, a record of
 * the call stack at a particular point in time) which can be printed later.
 * <p>
 * A {@code Throwable} can also include a cause, which is a nested {@code
 * Throwable} that represents the original problem that led to this {@code
 * Throwable}. It is often used for wrapping various types of errors into a
 * common {@code Throwable} without losing the detailed original error
 * information. When printing the stack trace, the trace of the cause is
 * included.
 * 
 * JUniversal REMOVED:
 * 
 * Currently for JUniversal there's no stack trace support; those methods were removed.  There's
 * no serialization support either & writeObject was removed.
 * 
 * Removed serialization support  
 *
 * @see Error
 * @see Exception
 * @see RuntimeException
 */
public class Throwable {

    /**
     * The message provided when the exception was created.
     */
    private String detailMessage;

    /**
     * The cause of this Throwable. Null when there is no cause.
     */
    private Throwable cause = this;

    /**
     * Constructs a new {@code Throwable} that includes the current stack trace.
     */
    public Throwable() {
        super();
        fillInStackTrace();
    }

    /**
     * Constructs a new {@code Throwable} with the current stack trace and the
     * specified detail message.
     *
     * @param detailMessage
     *            the detail message for this {@code Throwable}.
     */
    public Throwable(String detailMessage) {
        super();
        fillInStackTrace();
        this.detailMessage = detailMessage;
    }

    /**
     * Constructs a new {@code Throwable} with the current stack trace, the
     * specified detail message and the specified cause.
     * 
     * @param detailMessage
     *            the detail message for this {@code Throwable}.
     * @param throwable
     *            the cause of this {@code Throwable}.
     */
    public Throwable(String detailMessage, Throwable throwable) {
        super();
        fillInStackTrace();
        this.detailMessage = detailMessage;
        cause = throwable;
    }

    /**
     * Constructs a new {@code Throwable} with the current stack trace and the
     * specified cause.
     *
     * @param throwable
     *            the cause of this {@code Throwable}.
     */
    public Throwable(Throwable throwable) {
        super();
        fillInStackTrace();
        this.detailMessage = throwable == null ? null : throwable.toString();
        cause = throwable;
    }

    /*
	 * This native must be implemented to use the reference implementation of this class.
	 * 
	 * JUniversal CHANGE: Made this method a noop, not native. Hopefully, later we can add stack
	 * trace support of some kind.
	 */
    /**
     * Records the stack trace from the point where this method has been called
     * to this {@code Throwable}. The method is public so that code which
     * catches a {@code Throwable} and then re-throws it can adjust the stack
     * trace to represent the location where the exception was re-thrown.
     *
     * @return this {@code Throwable} instance.
     */
    //public native Throwable fillInStackTrace();
    public Throwable fillInStackTrace() { return this; }

    /**
     * Returns the extra information message which was provided when this
     * {@code Throwable} was created. Returns {@code null} if no message was
     * provided at creation time.
     *
     * @return this {@code Throwable}'s detail message.
     */
    public String getMessage() {
        return detailMessage;
    }

    /**
     * Returns the extra information message which was provided when this
     * {@code Throwable} was created. Returns {@code null} if no message was
     * provided at creation time. Subclasses may override this method to return
     * localized text for the message. The Android reference implementation
     * returns the unlocalized detail message.
     * 
     * @return this {@code Throwable}'s localized detail message.
     */
    public String getLocalizedMessage() {
        return getMessage();
    }

	/**
	 * JUniversal CHANGE: For JUniversal we don't support reflection so the
	 * derived class name isn't included in the toString representation; it just
	 * says Throwable plus the message.
	 */
    @Override
    public String toString() {
        String msg = getLocalizedMessage();
        String name = "Throwable";
        if (msg == null) {
            return name;
        }
        return new StringBuilder(name.length() + 2 + msg.length()).append(name).append(": ")
                .append(msg).toString();
    }

    /**
     * Initializes the cause of this {@code Throwable}. The cause can only be
     * initialized once.
     *
     * @param throwable
     *            the cause of this {@code Throwable}.
     * @return this {@code Throwable} instance.
     * @throws IllegalArgumentException
     *             if {@code Throwable} is this object.
     * @throws IllegalStateException
     *             if the cause has already been initialized.
     */
    public synchronized Throwable initCause(Throwable throwable) {
        if (cause == this) {
            if (throwable != this) {
                cause = throwable;
                return this;
            }
            throw new IllegalArgumentException("Cause cannot be the receiver");
        }
        throw new IllegalStateException("Cause already initialized");
    }

    /**
     * Returns the cause of this {@code Throwable}, or {@code null} if there is
     * no cause.
     * 
     * @return Throwable this {@code Throwable}'s cause.
     */
    public Throwable getCause() {
        if (cause == this) {
            return null;
        }
        return cause;
    }
}
