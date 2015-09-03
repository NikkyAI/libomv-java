/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2012-2015, Frederick Martian
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
package libomv.rendering;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ShortBuffer;

import libomv.types.Vector3;
import libomv.utils.Helpers;

import org.apache.commons.io.input.SwappedDataInputStream;

/**
 * A reference mesh is one way to implement level of detail
 *
 * @remarks
 * Reference meshes are supplemental meshes to full meshes. For all practical
 * purposes almost all lod meshes are implemented as reference meshes, except for 
 * 'avatar_eye_1.llm' which for some reason is implemented as a full mesh.
 */
public class ReferenceMesh
{
    static final protected String MESH_HEADER = "Linden Binary Mesh 1.0";
    static final protected String MORPH_FOOTER = "End Morphs";

    public class Face
    {
        public short Indices1;
        public short Indices2;
        public short Indices3;
        
        public Face(ShortBuffer indices, int idx)
        {
        	Indices1 = indices.get(idx++);
        	Indices2 = indices.get(idx++);
        	Indices3 = indices.get(idx++);
        }
    }

    public float MinPixelWidth;

    protected String _header;
    protected boolean _hasWeights;
    protected boolean _hasDetailTexCoords;
    protected Vector3 _position;
    protected Vector3 _rotationAngles;
    protected byte _rotationOrder;
    protected Vector3 _scale;
    protected short _numFaces;
    protected ShortBuffer _faces;

    public String getHeader() { return _header; }
    public boolean getHasWeights() { return _hasWeights; }
    public boolean getHasDetailTexCoords() { return _hasDetailTexCoords; }
    public Vector3 getPosition() { return _position; }
    public Vector3 getRotationAngles() { return _rotationAngles; }
    public byte getRotationOrder() { return _rotationOrder; }
    public Vector3 getScale() { return _scale; }

    public short getNumFaces() { return _numFaces; }
    public Face getFace(int index)
    {
    	if (index >= _numFaces)
    		return null;
		return new Face(_faces, index * 3);
    }

    public void load(String filename) throws IOException
    {
    	InputStream stream = new FileInputStream(filename);
    	try
    	{
    		load(stream);
    	}
    	finally
    	{
    		stream.close();
    	}
    }
    
    public void load(InputStream stream) throws IOException
    {
    	SwappedDataInputStream fis = new SwappedDataInputStream(stream);

    	load(fis);
        
        _numFaces = fis.readShort();
   		_faces = ShortBuffer.allocate(3 * _numFaces);
        for (int i = 0; i < _numFaces; i++)
        {
        	_faces.put(fis.readShort());
        	_faces.put(fis.readShort());
        	_faces.put(fis.readShort());
        }
    }
    
    protected void load(SwappedDataInputStream fis) throws IOException
    {
    	_header = Helpers.readString(fis, 24);
        if (!_header.equals(MESH_HEADER))
            throw new IOException("Unrecognized mesh format");

        // Populate base mesh variables
        _hasWeights = fis.readByte() != 1;
        _hasDetailTexCoords = fis.readByte() != 1;
        _position = new Vector3(fis);
        _rotationAngles = new Vector3(fis);
        _rotationOrder = fis.readByte();
        _scale = new Vector3(fis);
    }
}
