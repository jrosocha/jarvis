/*
  *
  *  *  Copyright 2014 Orient Technologies LTD (info(at)orientechnologies.com)
  *  *
  *  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  *  you may not use this file except in compliance with the License.
  *  *  You may obtain a copy of the License at
  *  *
  *  *       http://www.apache.org/licenses/LICENSE-2.0
  *  *
  *  *  Unless required by applicable law or agreed to in writing, software
  *  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  *  See the License for the specific language governing permissions and
  *  *  limitations under the License.
  *  *
  *  * For more information: http://www.orientechnologies.com
  *
  */
package com.jhr.jarvis.orientDb.functions;

import java.util.Iterator;
import java.util.LinkedList;

import com.orientechnologies.common.collection.OMultiValue;
import com.orientechnologies.common.types.OModifiableBoolean;
import com.orientechnologies.orient.core.command.OCommandContext;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.serialization.serializer.OStringSerializerHelper;
import com.orientechnologies.orient.core.sql.OSQLHelper;
import com.orientechnologies.orient.graph.sql.OGraphCommandExecutorSQLFactory;
import com.orientechnologies.orient.graph.sql.functions.OSQLFunctionPathFinder;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;


/**
 * Dijkstra's algorithm describes how to find the cheapest path from one node to another node in a directed weighted graph.
 * 
 * The first parameter is source record. The second parameter is destination record. The third parameter is a name of property that
 * represents 'weight'.
 * 
 * If property is not defined in edge or is null, distance between vertexes are 0.
 * 
 * @author Luca Garulli (l.garulli--at--orientechnologies.com)
 * 
 */
public class OSQLFunctionDijkstraWithWeightMax extends OSQLFunctionPathFinder {
  public static final String NAME = "dijkstra2";

  private String             paramWeightFieldName;
  private double             paramWeightLimit;

  public OSQLFunctionDijkstraWithWeightMax() {
    super(NAME, 4, 5);
  }
  
  public LinkedList<OrientVertex> execute(Object iThis, OIdentifiable iCurrentRecord, Object iCurrentResult,
      final Object[] iParams, OCommandContext iContext) {
    
  final OModifiableBoolean shutdownFlag = new OModifiableBoolean();
  final OrientBaseGraph graph = OGraphCommandExecutorSQLFactory.getGraph(false, shutdownFlag);

    final ORecord record = (ORecord) (iCurrentRecord != null ? iCurrentRecord.getRecord() : null);

    Object source = iParams[0];
    if (OMultiValue.isMultiValue(source)) {
      if (OMultiValue.getSize(source) > 1)
        throw new IllegalArgumentException("Only one sourceVertex is allowed");
      source = OMultiValue.getFirstValue(source);
    }
    paramSourceVertex = graph.getVertex(OSQLHelper.getValue(source, record, iContext));

    Object dest = iParams[1];
    if (OMultiValue.isMultiValue(dest)) {
      if (OMultiValue.getSize(dest) > 1)
        throw new IllegalArgumentException("Only one destinationVertex is allowed");
      dest = OMultiValue.getFirstValue(dest);
    }
    paramDestinationVertex = graph.getVertex(OSQLHelper.getValue(dest, record, iContext));

    paramWeightFieldName = OStringSerializerHelper.getStringContent(iParams[2]);
    
    paramWeightLimit = Double.parseDouble(iParams[3].toString());
    
    if (iParams.length > 4)
      paramDirection = Direction.valueOf(iParams[4].toString().toUpperCase());

    return super.execute(iContext);
  }

  public String getSyntax() {
    return "dijkstra2(<sourceVertex>, <destinationVertex>, <weightEdgeFieldName>, <weightLimit>, [<direction>])";
  }

  protected float getDistance(final OrientVertex node, final OrientVertex target) {
    final Iterator<Edge> edges = node.getEdges(target, paramDirection).iterator();
    if (edges.hasNext()) {
      final Edge e = edges.next();
      if (e != null && e.getLabel().equals("Frameshift") 
              && e.getProperty(paramWeightFieldName) != null 
              && ((double)e.getProperty(paramWeightFieldName) <=  paramWeightLimit)) {
 
        final Object fieldValue = e.getProperty(paramWeightFieldName);
        if (fieldValue != null) {
          if (fieldValue instanceof Float) {
            return (Float) fieldValue;
          } else if (fieldValue instanceof Number) {
            return ((Number) fieldValue).floatValue();
          }
        }
      }
      return Float.MAX_VALUE;
    }
    return Float.MAX_VALUE;
  }

  @Override
  protected boolean isVariableEdgeWeight() {
    return true;
  }
}