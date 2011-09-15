package libomv.utils;

public abstract class BitFlags<T> {
    
    private final long _mask;
    private long _value;

    /**
     * <p>Creates a BitFlags instance.</p>
     *
     * @param mask the mask specifying which bits apply to this
     *  BitFlags. Bits that are set in this mask are the bits
     *  that this BitFlags operates on
     */
    public BitFlags(long mask) {
        _mask = mask;
        _value = 0;
    }
    public BitFlags(long mask, long value) {
        _mask = mask;
        _value = value;
    }
    public BitFlags(BitFlags<T> flags) {
        _mask = flags._mask;
        _value = flags._value;
    }

    /**
     * <p>Obtains the value for the specified BitFlags.</p>
     *
     * <p>Many users of a BitFlags will want to treat the specified
     * bits as an int value, and will not want to be aware that the
     * value is stored as a BitFlags.</p>
     *
     * @see #setValue(long)
     * @return the internal integer value
     */
    protected long getLong() {
    	return _value;
    }
    protected int getInt() {
    	return (int)_value;
    }
    protected short getShort() {
    	return (short)_value;
    }
    protected byte getByte() {
    	return (byte)_value;
    }
    public abstract T getValue();
    
    /**
     * <p>Returns whether the field is set or not.</p>
     *
     * <p>This is most commonly used for a single-bit field, which is
     * often used to represent a boolean value; the results of using
     * it for a multi-bit field is to determine whether *any* of its
     * bits are set.</p>
     *
     * @param holder the int data containing the bits we're interested
     *  in
     * @return <code>true</code> if any of the bits are set,
     *  else <code>false</code>
     */
    public boolean isSet(long mask) {
        return (_value & mask) != 0;
    }

    /**
     * <p>Returns whether all of the bits are set or not.</p>
     *
     * <p>This is a stricter test than {@link #isSet(int)},
     * in that all of the bits in a multi-bit set must be set
     * for this method to return <code>true</code>.</p>
     *
     * @param holder the int data containing the bits we're
     *  interested in
     * @return <code>true</code> if all of the bits are set,
     *  else <code>false</code>
     */
    public boolean isAllSet(long mask) {
        return (_value & mask) == mask;
    }

    /**
     * <p>Replaces the bits with new values.</p>
     *
     * @see #getValue()
     * @param value the new value for the specified bits
     * @return the BitFlags containing the result
     */
    public BitFlags<T> setValue(long value) {
    	_value = (value & _mask);
    	return this;
    }

    /**
     * <p>Clears the bits.</p>
     *
     * @param value the BitFlags data containing the bits we're interested in
     * @return the BitFlags containing the result
     */
    public BitFlags<T> reset(BitFlags<T> value) {
        _value &= ~(value._value & _mask);
        return this;
    }
    public BitFlags<T> reset(long value) {
        _value &= ~(value & _mask);
        return this;
    }

    /**
     * <p>Sets the bits.</p>
     *
     * @param value the BitFlags data containing the bits we're interested in
     * @return the BitFlags containing the result
     */
    public BitFlags<T> set(BitFlags<T> value) {
        _value |= (value._value & _mask);
        return this;
    }
    public BitFlags<T> set(long value) {
        _value |= (value & _mask);
        return this;
    }

    /**
     * <p>Ands the bits.</p>
     *
     * @param value the BitFlags data containing the bits we're interested in
     * @return the BitFlags containing the result
     */
    public BitFlags<T> and(BitFlags<T> value) {
        _value &= (value._value & _mask);
        return this;
    }
    public BitFlags<T> and(long value) {
        _value &= (value & _mask);
        return this;
    }

    /**
     * <p>Inverts the bits.</p>
     *
     * @param value the BitFlags data containing the bits we're interested in
     * @return the BitFlags containing the result
     */
    public BitFlags<T> not() {
        _value = ~_value;
        return this;
    }

    /**
     * <p>Sets a boolean BitField.</p>
     *
     * @param value the BitFlags data containing the bits we're interested in
     * @param flag indicating whether to set or clear the bits
     * @return the BitFlags containing the result
     */
    public BitFlags<T> setBoolean(BitFlags<T> value, boolean flag) {
        if (flag)
        	set(value);
        else
        	reset(value);
        return this;
    }
    
    public BitFlags<T> setBoolean(long value, boolean flag) {
        if (flag)
        	set(value);
        else
        	reset(value);
        return this;
    }
 
    public boolean equals(BitFlags<T> value)
    {
    	return (_value == value._value);
    }
    
    public int hashCode()
    {
    	return (int)_value;
    }
}
