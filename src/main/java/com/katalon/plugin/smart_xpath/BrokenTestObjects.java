package com.katalon.plugin.smart_xpath;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BrokenTestObjects {

	@SerializedName("brokenTestObjects")
	@Expose
	private List<BrokenTestObject> brokenTestObjects = new ArrayList<>();

	public List<BrokenTestObject> getBrokenTestObjects() {
		return brokenTestObjects;
	}

	public void setBrokenTestObjects(List<BrokenTestObject> brokenTestObjects) {
		this.brokenTestObjects = brokenTestObjects;
	}
}