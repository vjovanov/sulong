/*
 * Copyright (c) 2017, 2018, Oracle and/or its affiliates.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.oracle.truffle.llvm.runtime.debug.type;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.llvm.runtime.debug.scope.LLVMSourceLocation;

import java.util.function.Supplier;

public final class LLVMSourceArrayLikeType extends LLVMSourceType {

    @CompilationFinal private LLVMSourceType baseType;
    @CompilationFinal private long length;

    public LLVMSourceArrayLikeType(long size, long align, long offset, LLVMSourceLocation location) {
        this(LLVMSourceType.UNKNOWN::getName, size, align, offset, LLVMSourceType.UNKNOWN, 1L, location);
    }

    private LLVMSourceArrayLikeType(Supplier<String> name, long size, long align, long offset, LLVMSourceType baseType, long length, LLVMSourceLocation location) {
        super(size, align, offset, location);
        setName(name);
        this.baseType = baseType;
        this.length = length;
    }

    public LLVMSourceType getBaseType() {
        return baseType;
    }

    public void setBaseType(LLVMSourceType baseType) {
        CompilerAsserts.neverPartOfCompilation();
        this.baseType = baseType;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        CompilerAsserts.neverPartOfCompilation();
        this.length = length;
    }

    @Override
    public LLVMSourceType getOffset(long newOffset) {
        return new LLVMSourceArrayLikeType(this::getName, getSize(), getAlign(), newOffset, baseType, length, getLocation());
    }

    @Override
    public boolean isAggregate() {
        return true;
    }

    @Override
    public int getElementCount() {
        return (int) getLength();
    }

    @Override
    public String getElementName(long i) {
        if (0 <= i && i < getLength()) {
            return IndexedTypeBounds.toKey(i);
        }
        return null;
    }

    @Override
    public LLVMSourceType getElementType(long i) {
        if (0 <= i && i < getLength()) {
            return baseType.getOffset(i * baseType.getSize());
        }
        return null;
    }

    @Override
    public LLVMSourceType getElementType(String key) {
        return getElementType(IndexedTypeBounds.toIndex(key));
    }

    @Override
    public LLVMSourceLocation getElementDeclaration(long i) {
        return getLocation();
    }

    @Override
    public LLVMSourceLocation getElementDeclaration(String name) {
        return getLocation();
    }
}
