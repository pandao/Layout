/**
 * 
 */
package com.efurture.marlin.el;

import java.util.List;

import com.efurture.marlin.GLog;


class ListValueResolver implements ValueResolver{


	@Override
	public boolean canResolve(Object target, Class<?> cls, String name) {
		return target instanceof List<?>;
	}


	@Override
	public Object resolve(Object target, Class<?> cls, String name) {
		List<?> list = (List<?>)target;
		try {
			int index = Integer.parseInt(name);
			return list.get(index);
		}
		catch (Exception e) {
			GLog.w(e.getMessage());
			return null;
		}
	}

}
