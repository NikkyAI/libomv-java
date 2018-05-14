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

import java.util.ArrayList;

import libomv.primMesher.types.ViewerFace;
import libomv.primMesher.types.ViewerPolygon;
import libomv.primMesher.types.ViewerVertex;

public class VertexIndexer
{
    public ArrayList<ArrayList<ViewerVertex>> viewerVertices;
    public ArrayList<ArrayList<ViewerPolygon>> viewerPolygons;
    public int numPrimFaces;
    private int[][] viewerVertIndices;

    public VertexIndexer()
    {
    }

    public VertexIndexer(PrimMesh primMesh)
    {
        int maxPrimFaceNumber = 0;

        for (ViewerFace vf : primMesh.viewerFaces)
            if (maxPrimFaceNumber < vf.primFaceNumber)
                maxPrimFaceNumber = vf.primFaceNumber;

        this.numPrimFaces = maxPrimFaceNumber + 1;

        int[] numViewerVerts = new int[numPrimFaces];
        int[] numVertsPerPrimFace = new int[numPrimFaces];

        for (int i = 0; i < numPrimFaces; i++)
        {
            numViewerVerts[i] = 0;
            numVertsPerPrimFace[i] = 0;
        }

        for (ViewerFace vf : primMesh.viewerFaces)
            numVertsPerPrimFace[vf.primFaceNumber] += 3;

        this.viewerVertices = new ArrayList<ArrayList<ViewerVertex>>(numPrimFaces);
        this.viewerPolygons = new ArrayList<ArrayList<ViewerPolygon>>(numPrimFaces);
        this.viewerVertIndices = new int[numPrimFaces][];

        // create index lists
        for (int primFaceNumber = 0; primFaceNumber < numPrimFaces; primFaceNumber++)
        {
            //set all indices to -1 to indicate an invalid index
            int[] vertIndices = new int[primMesh.coords.size()];
            for (int i = 0; i < primMesh.coords.size(); i++)
                vertIndices[i] = -1;
            viewerVertIndices[primFaceNumber] = vertIndices;
            
            viewerVertices.add(new ArrayList<ViewerVertex>(numVertsPerPrimFace[primFaceNumber]));
            viewerPolygons.add(new ArrayList<ViewerPolygon>());
        }

        // populate the index lists
        for (ViewerFace vf : primMesh.viewerFaces)
        {
            int v1, v2, v3;
            
            int[] vertIndices = viewerVertIndices[vf.primFaceNumber];
            ArrayList<ViewerVertex> viewerVerts = viewerVertices.get(vf.primFaceNumber);

            // add the vertices
            if (vertIndices[vf.coordIndex1] < 0)
            {
                viewerVerts.add(new ViewerVertex(vf.v1, vf.n1, vf.uv1));
                v1 = viewerVerts.size() - 1;
                vertIndices[vf.coordIndex1] = v1;
            }
            else v1 = vertIndices[vf.coordIndex1];

            if (vertIndices[vf.coordIndex2] < 0)
            {
                viewerVerts.add(new ViewerVertex(vf.v2, vf.n2, vf.uv2));
                v2 = viewerVerts.size() - 1;
                vertIndices[vf.coordIndex2] = v2;
            }
            else v2 = vertIndices[vf.coordIndex2];

            if (vertIndices[vf.coordIndex3] < 0)
            {
                viewerVerts.add(new ViewerVertex(vf.v3, vf.n3, vf.uv3));
                v3 = viewerVerts.size() - 1;
                vertIndices[vf.coordIndex3] = v3;
            }
            else v3 = vertIndices[vf.coordIndex3];

            viewerPolygons.get(vf.primFaceNumber).add(new ViewerPolygon(v1, v2, v3));
        }

    }
}
