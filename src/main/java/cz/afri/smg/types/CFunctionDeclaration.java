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
