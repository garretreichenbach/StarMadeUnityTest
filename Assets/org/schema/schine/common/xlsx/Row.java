package org.schema.schine.common.xlsx;

import java.util.Map;
import java.util.TreeMap;

public class Row {
public Integer id = null;
public Integer index = null;
public Row tmpRow = null;
public Style style = null;
public Double height = null;
public Map<Integer,Cell> cells = new TreeMap<Integer,Cell>();
public String spans = null;
public Integer customHeight = null;}
