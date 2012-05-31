/**
 * Copyright (c) 2008, openmetaverse.org
 * Copyright (c) 2009-2012, Frederick Martian
 * All rights reserved.
 *
 * - Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Neither the name of the openmetaverse.org nor the names
 *   of its contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
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
package libomv.rendering;

import java.util.ArrayList;
import java.util.List;

import libomv.types.Vector3;

public class SimpleMesh extends Mesh
{
    public List<Vertex> Vertices;
    public List<Short> Indices;

    public SimpleMesh()
    {
    }

    public SimpleMesh(SimpleMesh mesh)
    {
        this.Indices = new ArrayList<Short>(mesh.Indices);
        this.Path.Open = mesh.Path.Open;
        this.Path.Points = new ArrayList<PathPoint>(mesh.Path.Points);
        this.Prim = mesh.Prim;
        this.Profile.Concave = mesh.Profile.Concave;
        this.Profile.Faces = new ArrayList<ProfileFace>(mesh.Profile.Faces);
        this.Profile.MaxX = mesh.Profile.MaxX;
        this.Profile.MinX = mesh.Profile.MinX;
        this.Profile.Open = mesh.Profile.Open;
        this.Profile.Positions = new ArrayList<Vector3>(mesh.Profile.Positions);
        this.Profile.TotalOutsidePoints = mesh.Profile.TotalOutsidePoints;
        this.Vertices = new ArrayList<Vertex>(mesh.Vertices);
    }
}