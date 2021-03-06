/**
 * Copyright (c) 2002-2012 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.cypher.internal.commands.expressions

import collection.Seq
import org.neo4j.cypher.internal.pipes.aggregation._
import org.neo4j.cypher.internal.symbols._
import org.neo4j.cypher.SyntaxException
import collection.Map
import org.neo4j.helpers.ThisShouldNotHappenError
import org.neo4j.cypher.internal.pipes.ExecutionContext

abstract class AggregationExpression extends Expression {
  def apply(ctx: ExecutionContext) = throw new ThisShouldNotHappenError("Andres", "Aggregations should not be used like this.")

  def createAggregationFunction: AggregationFunction
}

abstract class AggregationWithInnerExpression(inner:Expression) extends AggregationExpression {
  if(inner.containsAggregate)
    throw new SyntaxException("Can't use aggregate functions inside of aggregate functions.")
  
  def expectedInnerType: CypherType
  
  def filter(f: (Expression) => Boolean) = if (f(this))
    Seq(this) ++ inner.filter(f)
  else
    inner.filter(f)

  def symbolTableDependencies = inner.symbolTableDependencies
}