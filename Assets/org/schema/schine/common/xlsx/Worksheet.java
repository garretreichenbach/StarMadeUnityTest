package org.schema.schine.common.xlsx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Worksheet {
public Integer id = null;
public String name = null;
public String dimension = null;
public Map<Integer, Row> rows = new TreeMap<Integer, Row>();
public Map<Integer, Column> columns = new TreeMap<Integer, Column>();
public List<Span> spans = new ArrayList<Span>();}
