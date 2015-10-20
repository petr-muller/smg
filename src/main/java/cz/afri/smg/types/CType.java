/*
 *  This file is part of SMG, a symbolic memory graph Java library
 *  Originally developed as part of CPAChecker, the configurable software verification platform
 *
 *  Copyright (C) 2011-2015  Petr Muller
 *  Copyright (C) 2007-2014  Dirk Beyer
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package cz.afri.smg.types;

public class CType {

	private static final int SIZE_INT = 4;
	private static final int SIZE_POINTER = 8;

	public static CType createTypeWithLength(final int pSizeInBytes) {
		return new CType(pSizeInBytes);
	}

	public static CType getIntType() {
		return createTypeWithLength(SIZE_INT);
	}

	public static CType getPointerType() {
    return createTypeWithLength(SIZE_POINTER);
  }

	public static CType unknownType() {
		return null;
	}

	private final int size;

	public CType(final int pSize) {
		size = pSize;
	}

	public final int getSize() {
		return size;
	}

	@Override
	public final String toString() {
		return size + "b type";
	}

	@Override
	public final int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + size;
		return result;
	}

	@Override
	public final boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CType other = (CType) obj;
		if (size != other.size) {
			return false;
		}
		return true;
	}
}
