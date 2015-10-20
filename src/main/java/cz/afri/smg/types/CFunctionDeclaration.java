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

import com.google.common.collect.ImmutableList;

public class CFunctionDeclaration {
  private final CFunctionType functionType;
  private final String name;
  private final ImmutableList<CParameterDeclaration> params;
	public CFunctionDeclaration(final CFunctionType pFunctionType, final String pName,
	                            final ImmutableList<CParameterDeclaration> pParameters) {
		functionType = pFunctionType;
		name = pName;
		params = pParameters;
	}

  public final CFunctionType getType() {
		return functionType;
	}

  public final String getName() {
		// TODO Auto-generated method stub
		return name;
	}

  public final ImmutableList<CParameterDeclaration> getParams() {
    return params;
  }

  @Override
  public final String toString() {
    return functionType + " " + name + "(" + params + ")";
  }
}
