package com.osiris.headlessbrowser.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This is a {@link InputStream} which means
 * that you can read stuff from it. <br>
 * This bad boy however enables you to write stuff to it. <br>
 * Useful when you need a fake user input stream for example. <br>
 */
public class VirtualInputStream extends InputStream {

    public List<VirtualInputStream> copies = new CopyOnWriteArrayList<>();

    /**
     * An array of bytes that was provided
     * by the creator of the stream. Elements <code>buf[0]</code>
     * through <code>buf[count-1]</code> are the
     * only bytes that can ever be read from the
     * stream;  element <code>buf[pos]</code> is
     * the next byte to be read.
     */
    protected byte[] buf;

    /**
     * The index of the next character to read from the input stream buffer.
     * This value should always be nonnegative
     * and not larger than the value of <code>count</code>.
     * The next byte to be read from the input stream buffer
     * will be <code>buf[pos]</code>.
     */
    protected int pos;

    /**
     * The currently marked position in the stream.
     * ByteArrayInputStream objects are marked at position zero by
     * default when constructed.  They may be marked at another
     * position within the buffer by the <code>mark()</code> method.
     * The current buffer position is set to this point by the
     * <code>reset()</code> method.
     * <p>
     * If no mark has been set, then the value of mark is the offset
     * passed to the constructor (or 0 if the offset was not supplied).
     *
     * @since JDK1.1
     */
    protected int mark = 0;

    /**
     * The index one greater than the last valid character in the input
     * stream buffer.
     * This value should always be nonnegative
     * and not larger than the length of <code>buf</code>.
     * It  is one greater than the position of
     * the last byte within <code>buf</code> that
     * can ever be read  from the input stream buffer.
     */
    protected int count;

    public VirtualInputStream() {
        this(new byte[]{}); // Empty byte array to start with
    }

    public VirtualInputStream(byte[] buf) {
        this.buf = buf;
        this.pos = 0;
        this.count = buf.length;
    }

    /**
     * Duplicates the current {@link VirtualInputStream} and returns it. <br>
     * Aka it creates a copy of itself. Useful if you want to read one stream from multiple readers. <br>
     */
    public VirtualInputStream copy() {
        VirtualInputStream copy = new VirtualInputStream();
        copies.add(copy);
        return copy;
    }

    public void addLine(String line) {
        synchronized (this) {
            line = line + "\n";
            byte[] bytesLine = line.getBytes(StandardCharsets.UTF_8);
            int newLength = buf.length + bytesLine.length;
            byte[] newBuf = new byte[newLength];
            for (int i = 0; i < buf.length; i++) {
                newBuf[i] = buf[i];
            }
            int a = 0;
            int startIndex = 0;
            if (buf.length > 0) startIndex = buf.length;
            for (int i = startIndex; i < newLength; i++) {
                newBuf[i] = bytesLine[a];
                a++;
            }
            this.buf = newBuf;
            this.count = newBuf.length;
            for (VirtualInputStream copy :
                    copies) {
                copy.addLine(line);
            }
        }
    }

    public void addBytes(byte[] bytes) {
        Objects.requireNonNull(bytes);
        Objects.requireNonNull(buf);
        synchronized (this) {
            int newLength = buf.length + bytes.length;
            byte[] newBuf = new byte[newLength];
            for (int i = 0; i < buf.length; i++) {
                newBuf[i] = buf[i];
            }
            int a = 0;
            int startIndex = 0;
            if (buf.length > 0) startIndex = buf.length;
            for (int i = startIndex; i < newLength; i++) {
                newBuf[i] = bytes[a];
                a++;
            }
            this.buf = newBuf;
            this.count = newBuf.length;
            for (VirtualInputStream copy :
                    copies) {
                copy.addBytes(bytes);
            }
        }
    }

    /**
     * Blocks until there is a line to read.
     */
    public String readLine() throws IOException {
        StringBuilder builder = new StringBuilder();
        int c = 0;
        while ((c = read()) != 10 && c != -1) // Until next line char or eof
            builder.append((char) c);
        return builder.toString();
    }

    /**
     * Reads the next byte of data from this input stream. The value
     * byte is returned as an <code>int</code> in the range
     * <code>0</code> to <code>255</code>. If no byte is available
     * because the end of the stream has been reached, the value
     * <code>-1</code> is returned.
     * <p>
     * This <code>read</code> method
     * cannot block.
     *
     * @return the next byte of data, or <code>-1</code> if the end of the
     * stream has been reached.
     */
    public synchronized int read() {
        return (pos < count) ? (buf[pos++] & 0xff) : -1;
    }

    /**
     * Reads up to <code>len</code> bytes of data into an array of bytes
     * from this input stream.
     * If <code>pos</code> equals <code>count</code>,
     * then <code>-1</code> is returned to indicate
     * end of file. Otherwise, the  number <code>k</code>
     * of bytes read is equal to the smaller of
     * <code>len</code> and <code>count-pos</code>.
     * If <code>k</code> is positive, then bytes
     * <code>buf[pos]</code> through <code>buf[pos+k-1]</code>
     * are copied into <code>b[off]</code>  through
     * <code>b[off+k-1]</code> in the manner performed
     * by <code>System.arraycopy</code>. The
     * value <code>k</code> is added into <code>pos</code>
     * and <code>k</code> is returned.
     * <p>
     * This <code>read</code> method cannot block.
     *
     * @param b   the buffer into which the data is read.
     * @param off the start offset in the destination array <code>b</code>
     * @param len the maximum number of bytes read.
     * @return the total number of bytes read into the buffer, or
     * <code>-1</code> if there is no more data because the end of
     * the stream has been reached.
     * @throws NullPointerException      If <code>b</code> is <code>null</code>.
     * @throws IndexOutOfBoundsException If <code>off</code> is negative,
     *                                   <code>len</code> is negative, or <code>len</code> is greater than
     *                                   <code>b.length - off</code>
     */
    public synchronized int read(byte[] b, int off, int len) {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        }

        if (pos >= count) {
            return -1;
        }

        int avail = count - pos;
        if (len > avail) {
            len = avail;
        }
        if (len <= 0) {
            return 0;
        }
        System.arraycopy(buf, pos, b, off, len);
        pos += len;
        return len;
    }

    /**
     * Skips <code>n</code> bytes of input from this input stream. Fewer
     * bytes might be skipped if the end of the input stream is reached.
     * The actual number <code>k</code>
     * of bytes to be skipped is equal to the smaller
     * of <code>n</code> and  <code>count-pos</code>.
     * The value <code>k</code> is added into <code>pos</code>
     * and <code>k</code> is returned.
     *
     * @param n the number of bytes to be skipped.
     * @return the actual number of bytes skipped.
     */
    public synchronized long skip(long n) {
        long k = count - pos;
        if (n < k) {
            k = n < 0 ? 0 : n;
        }

        pos += k;
        return k;
    }

    /**
     * Returns the number of remaining bytes that can be read (or skipped over)
     * from this input stream.
     * <p>
     * The value returned is <code>count&nbsp;- pos</code>,
     * which is the number of bytes remaining to be read from the input buffer.
     *
     * @return the number of remaining bytes that can be read (or skipped
     * over) from this input stream without blocking.
     */
    public synchronized int available() {
        return count - pos;
    }

    /**
     * Tests if this <code>InputStream</code> supports mark/reset. The
     * <code>markSupported</code> method of <code>ByteArrayInputStream</code>
     * always returns <code>true</code>.
     *
     * @since JDK1.1
     */
    public boolean markSupported() {
        return true;
    }

    /**
     * Set the current marked position in the stream.
     * ByteArrayInputStream objects are marked at position zero by
     * default when constructed.  They may be marked at another
     * position within the buffer by this method.
     * <p>
     * If no mark has been set, then the value of the mark is the
     * offset passed to the constructor (or 0 if the offset was not
     * supplied).
     *
     * <p> Note: The <code>readAheadLimit</code> for this class
     * has no meaning.
     *
     * @since JDK1.1
     */
    public void mark(int readAheadLimit) {
        mark = pos;
    }

    /**
     * Resets the buffer to the marked position.  The marked position
     * is 0 unless another position was marked or an offset was specified
     * in the constructor.
     */
    public synchronized void reset() {
        pos = mark;
    }

    /**
     * Closing a <tt>ByteArrayInputStream</tt> has no effect. The methods in
     * this class can be called after the stream has been closed without
     * generating an <tt>IOException</tt>.
     */
    public void close() throws IOException {
    }
}