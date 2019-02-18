package com.katalon.plugin.smart_xpath.entity;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BrokenTestObjects {

	@SerializedName("brokenTestObjects")
	@Expose
	private Set<BrokenTestObject> brokenTestObjects = new HashSet<>();

	public Set<BrokenTestObject> getBrokenTestObjects() {
		return brokenTestObjects;
	}

	public void setBrokenTestObjects(Set<BrokenTestObject> brokenTestObjects) {
		this.brokenTestObjects = brokenTestObjects;
	}
}