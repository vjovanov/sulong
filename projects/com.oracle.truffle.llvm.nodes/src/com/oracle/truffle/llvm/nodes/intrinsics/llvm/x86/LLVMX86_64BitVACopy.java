/*
 * Copyright (c) 2016, 2018, Oracle and/or its affiliates.
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
package com.oracle.truffle.llvm.nodes.intrinsics.llvm.x86;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.llvm.nodes.intrinsics.llvm.LLVMBuiltin;
import com.oracle.truffle.llvm.nodes.memory.LLVMGetElementPtrNode.LLVMIncrementPointerNode;
import com.oracle.truffle.llvm.nodes.memory.LLVMGetElementPtrNodeGen.LLVMIncrementPointerNodeGen;
import com.oracle.truffle.llvm.nodes.memory.load.LLVMDirectLoadNodeFactory.LLVMPointerDirectLoadNodeGen;
import com.oracle.truffle.llvm.nodes.memory.load.LLVMI32LoadNodeGen;
import com.oracle.truffle.llvm.nodes.memory.store.LLVMI32StoreNodeGen;
import com.oracle.truffle.llvm.nodes.memory.store.LLVMPointerStoreNodeGen;
import com.oracle.truffle.llvm.runtime.nodes.api.LLVMExpressionNode;
import com.oracle.truffle.llvm.runtime.nodes.api.LLVMLoadNode;
import com.oracle.truffle.llvm.runtime.nodes.api.LLVMStoreNode;

@NodeChildren({@NodeChild(type = LLVMExpressionNode.class), @NodeChild(type = LLVMExpressionNode.class)})
@NodeField(type = int.class, name = "numberExplicitArguments")
public abstract class LLVMX86_64BitVACopy extends LLVMBuiltin {

    @Child private LLVMIncrementPointerNode pointerArithmeticStructInit;
    @Child private LLVMStoreNode gpOffsetStore;
    @Child private LLVMStoreNode fpOffsetStore;
    @Child private LLVMStoreNode overflowArgAreaStore;
    @Child private LLVMStoreNode regSaveAreaStore;

    @Child private LLVMLoadNode gpOffsetLoad;
    @Child private LLVMLoadNode fpOffsetLoad;
    @Child private LLVMLoadNode overflowArgAreaLoad;
    @Child private LLVMLoadNode regSaveAreaLoad;

    public LLVMX86_64BitVACopy() {
        this.pointerArithmeticStructInit = LLVMIncrementPointerNodeGen.create();
        this.gpOffsetStore = LLVMI32StoreNodeGen.create(null, null);
        this.fpOffsetStore = LLVMI32StoreNodeGen.create(null, null);
        this.overflowArgAreaStore = LLVMPointerStoreNodeGen.create(null, null);
        this.regSaveAreaStore = LLVMPointerStoreNodeGen.create(null, null);

        this.gpOffsetLoad = LLVMI32LoadNodeGen.create(null);
        this.fpOffsetLoad = LLVMI32LoadNodeGen.create(null);
        this.overflowArgAreaLoad = LLVMPointerDirectLoadNodeGen.create(null);
        this.regSaveAreaLoad = LLVMPointerDirectLoadNodeGen.create(null);
    }

    private void setGPOffset(Object address, int value) {
        Object p = pointerArithmeticStructInit.executeWithTarget(address, X86_64BitVarArgs.GP_OFFSET);
        gpOffsetStore.executeWithTarget(p, value);
    }

    private void setFPOffset(Object address, int value) {
        Object p = pointerArithmeticStructInit.executeWithTarget(address, X86_64BitVarArgs.FP_OFFSET);
        fpOffsetStore.executeWithTarget(p, value);
    }

    private void setOverflowArgArea(Object address, Object value) {
        Object p = pointerArithmeticStructInit.executeWithTarget(address, X86_64BitVarArgs.OVERFLOW_ARG_AREA);
        overflowArgAreaStore.executeWithTarget(p, value);
    }

    private void setRegSaveArea(Object address, Object value) {
        Object p = pointerArithmeticStructInit.executeWithTarget(address, X86_64BitVarArgs.REG_SAVE_AREA);
        regSaveAreaStore.executeWithTarget(p, value);
    }

    private int getGPOffset(Object address) {
        Object p = pointerArithmeticStructInit.executeWithTarget(address, X86_64BitVarArgs.GP_OFFSET);
        return (int) gpOffsetLoad.executeWithTarget(p);
    }

    private int getFPOffset(Object address) {
        Object p = pointerArithmeticStructInit.executeWithTarget(address, X86_64BitVarArgs.FP_OFFSET);
        return (int) fpOffsetLoad.executeWithTarget(p);
    }

    private Object getOverflowArgArea(Object address) {
        Object p = pointerArithmeticStructInit.executeWithTarget(address, X86_64BitVarArgs.OVERFLOW_ARG_AREA);
        return overflowArgAreaLoad.executeWithTarget(p);
    }

    private Object getRegSaveArea(Object address) {
        Object p = pointerArithmeticStructInit.executeWithTarget(address, X86_64BitVarArgs.REG_SAVE_AREA);
        return regSaveAreaLoad.executeWithTarget(p);
    }

    public abstract int getNumberExplicitArguments();

    @Specialization
    protected Object doVoid(Object dest, Object source) {

        /*
         * COPY THIS: typedef struct { unsigned int gp_offset; unsigned int fp_offset; void
         * *overflow_arg_area; void *reg_save_area; } va_list[1];
         */
        setGPOffset(dest, getGPOffset(source));
        setFPOffset(dest, getFPOffset(source));
        setOverflowArgArea(dest, getOverflowArgArea(source));
        setRegSaveArea(dest, getRegSaveArea(source));

        return null;
    }
}
