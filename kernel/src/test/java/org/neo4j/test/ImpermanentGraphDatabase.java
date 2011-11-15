/**
 * Copyright (c) 2002-2011 "Neo Technology,"
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
package org.neo4j.test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.HighlyConfigurableGraphDatabase;
import org.neo4j.kernel.impl.EphemeralFileSystemAbstraction;
import org.neo4j.kernel.impl.EphemeralIdGenerator;
import org.neo4j.kernel.impl.util.FileUtils;
import org.neo4j.kernel.impl.util.StringLogger;

/**
 * A database meant to be used in unit tests. It will always be empty on start.
 */
public class ImpermanentGraphDatabase extends HighlyConfigurableGraphDatabase
{
    public static final AtomicLong instances = new AtomicLong();
    private static final File PATH = new File( "target/test-data/impermanent-db" );

    public ImpermanentGraphDatabase( Map<String, String> params )
    {
        super( path(), params, new EphemeralIdGenerator.Factory(),
                new EphemeralFileSystemAbstraction() );
        instances.incrementAndGet();
    }
    
    @Override
    protected StringLogger createStringLogger()
    {
        return StringLogger.DEV_NULL;
    }

    public ImpermanentGraphDatabase()
    {
        this( new HashMap<String, String>() );
    }
    
    private static String path()
    {
        clearDirectory();
        return PATH.getAbsolutePath();
    }

    private static void clearDirectory()
    {
        try
        {
            FileUtils.deleteRecursively( PATH );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    protected void close()
    {
        super.close();
        ((EphemeralFileSystemAbstraction) fileSystem).dispose();
        instances.decrementAndGet();
//        clearDirectory();
    }

    public void cleanContent( boolean retainReferenceNode )
    {
        Transaction tx = beginTx();
        try
        {
            for ( Node node : getAllNodes() )
            {
                for ( Relationship rel : node.getRelationships( Direction.OUTGOING ) )
                {
                    rel.delete();
                }
                if ( !node.hasRelationship() )
                {
                    if ( retainReferenceNode )
                    {
                        try
                        {
                            Node referenceNode = getReferenceNode();
                            if ( !node.equals( referenceNode ) )
                            {
                                node.delete();
                            }
                        }
                        catch ( NotFoundException nfe )
                        {
                            // no ref node
                        }
                    }
                    else
                    {
                        node.delete();
                    }
                }
            }
            tx.success();
        }
        catch ( Exception e )
        {
            tx.failure();
        }
        finally
        {
            tx.finish();
        }
    }

    public void cleanContent()
    {
        cleanContent( false );
    }
}
