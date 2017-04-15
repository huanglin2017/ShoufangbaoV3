package com.kupangstudio.shoufangbao.utils;

import java.util.Comparator;
import com.kupangstudio.shoufangbao.model.SelectCity;

public class CityComparator implements Comparator<SelectCity> {

	@Override
	public int compare(SelectCity o1, SelectCity o2) {
		if (o1.getInitial().equals("@") || o2.getInitial().equals("#")) {
			return -1;
		} else if (o1.getInitial().equals("#")
				|| o2.getInitial().equals("@")) {
			return 1;
		} else {
			return o1.getInitial().compareTo(o2.getInitial());
		}
	}

}
