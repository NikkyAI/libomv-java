/**
 * Copyright (c) 2010-2012, Dahlia Trimble
 * Copyright (c) 2011-2017, Frederick Martian
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the name of the openmetaverse.org or libomv-java project nor the
 *   names of its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package libomv.primMesher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

import libomv.primMesher.types.ViewerPolygon;
import libomv.primMesher.types.ViewerVertex;
import libomv.types.Vector2;
import libomv.types.Vector3;
import libomv.utils.HashMapInt;
import libomv.utils.Helpers;

public class ObjMesh
{
	ArrayList<Vector3> coords = new ArrayList<Vector3>();
	ArrayList<Vector3> normals = new ArrayList<Vector3>();
	ArrayList<Vector2> uvs = new ArrayList<Vector2>();

    public String meshName = Helpers.EmptyString;
    public ArrayList<ArrayList<ViewerVertex>> viewerVertices = new ArrayList<ArrayList<ViewerVertex>>();
    public ArrayList<ArrayList<ViewerPolygon>> viewerPolygons = new ArrayList<ArrayList<ViewerPolygon>>();

    ArrayList<ViewerVertex> faceVertices = new ArrayList<ViewerVertex>();
    ArrayList<ViewerPolygon> facePolygons = new ArrayList<ViewerPolygon>();
    public int numPrimFaces;

    HashMapInt<Integer> viewerVertexLookup = new HashMapInt<Integer>();

    public ObjMesh(String path) throws IOException
    {
    	BufferedReader br = new BufferedReader(new FileReader(new File(path))); 
        try
        {
        	processStream(br);
        }
        finally
        {
        	br.close();
        }
    }

    public ObjMesh(Reader sr) throws IOException
    {
    	BufferedReader br = sr instanceof BufferedReader ? (BufferedReader)sr : new BufferedReader(sr);
       	processStream(br);
    }

    private void processStream(BufferedReader s) throws IOException
    {
        numPrimFaces = 0;
        String line;
        do
        {
            line = s.readLine().trim();
            if (line != null)
            {
                String[] tokens = line.split(" +");

                // Skip blank lines and comments
                if (tokens.length > 0 && tokens[0].isEmpty() && !tokens[0].startsWith("#"))
                    processTokens(tokens);
            }
        }
        while (line != null);
        makePrimFace();
    }

    public VertexIndexer getVertexIndexer()
    {
        VertexIndexer vi = new VertexIndexer();
        vi.numPrimFaces = this.numPrimFaces;
        vi.viewerPolygons = this.viewerPolygons;
        vi.viewerVertices = this.viewerVertices;

        return vi;
    }


    private void processTokens(String[] tokens)
    {
    	String token = tokens[0].toLowerCase();
    	if (token.equals("o"))
    	{
            meshName = tokens[1];
    	}
    	else if (token.equals("v"))
    	{
            coords.add(parseCoord(tokens));
    	}
    	else if (token.equals("vt"))
    	{
    	    uvs.add(parseUVCoord(tokens));
    	}
    	else if (token.equals("vn"))
    	{
    	    normals.add(parseCoord(tokens));
    	}
    	else if (token.equals("g"))
    	{
    	     makePrimFace();
    	}
    	else if (token.equals("s"))
    	{
    		
    	}
    	else if (token.equals("f"))
    	{
            int[] vertIndices = new int[3];

            for (int vertexIndex = 1; vertexIndex <= 3; vertexIndex++)
            {
                String[] indices = tokens[vertexIndex].split("/");

                int positionIndex = Integer.parseInt(indices[0]) - 1;
                int texCoordIndex = -1;
                int normalIndex = -1;

                if (indices.length > 1)
                {
                	try
                	{
                		texCoordIndex = Integer.parseInt(indices[1]) - 1;
                	}
                	catch (NumberFormatException ex)
                	{
                		texCoordIndex = -1;
                	}
                }

                if (indices.length > 2)
                {
                	try
                	{
                		normalIndex = Integer.parseInt(indices[2]) - 1;
                	}
                	catch (NumberFormatException ex)
                	{
                		normalIndex = -1;
                	}
                 }

                int hash = hashInts(positionIndex, texCoordIndex, normalIndex);

                if (viewerVertexLookup.containsKey(hash))
                    vertIndices[vertexIndex - 1] = viewerVertexLookup.get(hash);
                else
                {
                    ViewerVertex vv = new ViewerVertex();
                    vv.v = coords.get(positionIndex);
                    if (normalIndex > -1)
                        vv.n = normals.get(normalIndex);
                    if (texCoordIndex > -1)
                        vv.uv = uvs.get(texCoordIndex);
                    faceVertices.add(vv);
                    vertIndices[vertexIndex - 1] = faceVertices.size() - 1;
                    viewerVertexLookup.put(hash, faceVertices.size() - 1);
                }
            }
            facePolygons.add(new ViewerPolygon(vertIndices[0], vertIndices[1], vertIndices[2]));
    	}
    	else if (token.equals("mtllib"))
    	{
    	}
    	else if (token.equals("usemtl"))
    	{
    	}
    	else
    	{
        }
    }


    private void makePrimFace()
    {
        if (faceVertices.size() > 0 && facePolygons.size() > 0)
        {
            viewerVertices.add(faceVertices);
            faceVertices = new ArrayList<ViewerVertex>();
            viewerPolygons.add(facePolygons);

            facePolygons = new ArrayList<ViewerPolygon>();

            viewerVertexLookup = new HashMapInt<Integer>();

            numPrimFaces++;
        }
    }

    private Vector2 parseUVCoord(String[] tokens)
    {
        return new Vector2(Float.valueOf(tokens[1]),
                           Float.valueOf(tokens[2]));
    }

    private Vector3 parseCoord(String[] tokens)
    {
        return new Vector3(Float.valueOf(tokens[1]),
        		           Float.valueOf(tokens[2]),
        		           Float.valueOf(tokens[3]));
    }

    private int hashInts(int i1, int i2, int i3)
    {
        return (Integer.toString(i1) + " " + Integer.toString(i2) + " " + Integer.toString(i3)).hashCode();
    }
}