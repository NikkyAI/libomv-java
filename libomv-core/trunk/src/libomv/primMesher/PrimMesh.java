/**
 * Copyright (c) 2010-2012, Dahlia Trimble
 * Copyright (c) 2011-2013, Frederick Martian
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import libomv.primMesher.types.Face;
import libomv.primMesher.types.Profile;
import libomv.primMesher.types.ViewerFace;
import libomv.types.Quaternion;
import libomv.types.Vector3;
import libomv.utils.Helpers;

public class PrimMesh
{
    public class PathNode
    {
        public Vector3 position;
        public Quaternion rotation;
        public float xScale;
        public float yScale;
        public float percentOfPath;
    }
   
    public enum PathType { Linear, Circular, Flexible }

    public class Path
    {
        public ArrayList<PathNode> pathNodes = new ArrayList<PathNode>();

        public float twistBegin = 0.0f;
        public float twistEnd = 0.0f;
        public float topShearX = 0.0f;
        public float topShearY = 0.0f;
        public float pathCutBegin = 0.0f;
        public float pathCutEnd = 1.0f;
        public float dimpleBegin = 0.0f;
        public float dimpleEnd = 1.0f;
        public float skew = 0.0f;
        public float holeSizeX = 1.0f; // called pathScaleX in pbs
        public float holeSizeY = 0.25f;
        public float taperX = 0.0f;
        public float taperY = 0.0f;
        public float radius = 0.0f;
        public float revolutions = 1.0f;
        public int stepsPerRevolution = 24;

        public void create(PathType pathType, int steps)
        {
            if (this.taperX > 0.999f)
                this.taperX = 0.999f;
            if (this.taperX < -0.999f)
                this.taperX = -0.999f;
            if (this.taperY > 0.999f)
                this.taperY = 0.999f;
            if (this.taperY < -0.999f)
                this.taperY = -0.999f;

            if (pathType == PathType.Linear || pathType == PathType.Flexible)
            {
                int step = 0;

                float length = this.pathCutEnd - this.pathCutBegin;
                float twistTotal = twistEnd - twistBegin;
                float twistTotalAbs = Math.abs(twistTotal);
                if (twistTotalAbs > 0.01f)
                    steps += (int)(twistTotalAbs * 3.66); //  dahlia's magic number

                float start = -0.5f;
                float stepSize = length / steps;
                float percentOfPathMultiplier = stepSize * 0.999999f;
                float xOffset = this.topShearX * this.pathCutBegin;
                float yOffset = this.topShearY * this.pathCutBegin;
                float zOffset = start;
                float xOffsetStepIncrement = this.topShearX * length / steps;
                float yOffsetStepIncrement = this.topShearY * length / steps;

                float percentOfPath = this.pathCutBegin;
                zOffset += percentOfPath;

                // sanity checks

                boolean done = false;

                while (!done)
                {
                    PathNode newNode = new PathNode();

                    newNode.xScale = 1.0f;
                    if (this.taperX == 0.0f)
                        newNode.xScale = 1.0f;
                    else if (this.taperX > 0.0f)
                        newNode.xScale = 1.0f - percentOfPath * this.taperX;
                    else newNode.xScale = 1.0f + (1.0f - percentOfPath) * this.taperX;

                    newNode.yScale = 1.0f;
                    if (this.taperY == 0.0f)
                        newNode.yScale = 1.0f;
                    else if (this.taperY > 0.0f)
                        newNode.yScale = 1.0f - percentOfPath * this.taperY;
                    else newNode.yScale = 1.0f + (1.0f - percentOfPath) * this.taperY;

                    float twist = twistBegin + twistTotal * percentOfPath;

                    newNode.rotation = new Quaternion(Vector3.UnitZ, twist);
                    newNode.position = new Vector3(xOffset, yOffset, zOffset);
                    newNode.percentOfPath = percentOfPath;

                    pathNodes.add(newNode);

                    if (step < steps)
                    {
                        step += 1;
                        percentOfPath += percentOfPathMultiplier;
                        xOffset += xOffsetStepIncrement;
                        yOffset += yOffsetStepIncrement;
                        zOffset += stepSize;
                        if (percentOfPath > this.pathCutEnd)
                            done = true;
                    }
                    else done = true;
                }
            } // end of linear path code

            else // pathType == Circular
            {
                float twistTotal = twistEnd - twistBegin;

                // if the profile has a lot of twist, add more layers otherwise the layers may overlap
                // and the resulting mesh may be quite inaccurate. This method is arbitrary and doesn't
                // accurately match the viewer
                float twistTotalAbs = Math.abs(twistTotal);
                if (twistTotalAbs > 0.01f)
                {
                    if (twistTotalAbs > Math.PI * 1.5f)
                        steps *= 2;
                    if (twistTotalAbs > Math.PI * 3.0f)
                        steps *= 2;
                }

                float yPathScale = this.holeSizeY * 0.5f;
                float pathLength = this.pathCutEnd - this.pathCutBegin;
                float totalSkew = this.skew * 2.0f * pathLength;
                float skewStart = this.pathCutBegin * 2.0f * this.skew - this.skew;
                float xOffsetTopShearXFactor = this.topShearX * (0.25f + 0.5f * (0.5f - this.holeSizeY));
                float yShearCompensation = 1.0f + Math.abs(this.topShearY) * 0.25f;

                // It's not quite clear what pushY (Y top shear) does, but subtracting it from the start and end
                // angles appears to approximate it's effects on path cut. Likewise, adding it to the angle used
                // to calculate the sine for generating the path radius appears to approximate it's effects there
                // too, but there are some subtle differences in the radius which are noticeable as the prim size
                // increases and it may affect megaprims quite a bit. The effect of the Y top shear parameter on
                // the meshes generated with this technique appear nearly identical in shape to the same prims when
                // displayed by the viewer.

                float startAngle = (Helpers.TWO_PI * this.pathCutBegin * this.revolutions) - this.topShearY * 0.9f;
                float endAngle = (Helpers.TWO_PI * this.pathCutEnd * this.revolutions) - this.topShearY * 0.9f;
                float stepSize = Helpers.TWO_PI / this.stepsPerRevolution;

                int step = (int)(startAngle / stepSize);
                float angle = startAngle;

                boolean done = false;
                while (!done) // loop through the length of the path and add the layers
                {
                    PathNode newNode = new PathNode();

                    float xProfileScale = (1.0f - Math.abs(this.skew)) * this.holeSizeX;
                    float yProfileScale = this.holeSizeY;

                    float percentOfPath = angle / (Helpers.TWO_PI * this.revolutions);
                    float percentOfAngles = (angle - startAngle) / (endAngle - startAngle);

                    if (this.taperX > 0.01f)
                        xProfileScale *= 1.0f - percentOfPath * this.taperX;
                    else if (this.taperX < -0.01f)
                        xProfileScale *= 1.0f + (1.0f - percentOfPath) * this.taperX;

                    if (this.taperY > 0.01f)
                        yProfileScale *= 1.0f - percentOfPath * this.taperY;
                    else if (this.taperY < -0.01f)
                        yProfileScale *= 1.0f + (1.0f - percentOfPath) * this.taperY;

                    newNode.xScale = xProfileScale;
                    newNode.yScale = yProfileScale;

                    float radiusScale = 1.0f;
                    if (this.radius > 0.001f)
                        radiusScale = 1.0f - this.radius * percentOfPath;
                    else if (this.radius < 0.001f)
                        radiusScale = 1.0f + this.radius * (1.0f - percentOfPath);

                    float twist = twistBegin + twistTotal * percentOfPath;

                    float xOffset = 0.5f * (skewStart + totalSkew * percentOfAngles);
                    xOffset += (float)Math.sin(angle) * xOffsetTopShearXFactor;

                    float yOffset = yShearCompensation * (float)Math.cos(angle) * (0.5f - yPathScale) * radiusScale;

                    float zOffset = (float)Math.sin(angle + this.topShearY) * (0.5f - yPathScale) * radiusScale;

                    newNode.position = new Vector3(xOffset, yOffset, zOffset);

                    // now orient the rotation of the profile layer relative to it's position on the path
                    // adding taperY to the angle used to generate the quat appears to approximate the viewer

                    newNode.rotation = new Quaternion(Vector3.UnitX, angle + this.topShearY);

                    // next apply twist rotation to the profile layer
                    if (twistTotal != 0.0f || twistBegin != 0.0f)
                        newNode.rotation = Quaternion.multiply(newNode.rotation, new Quaternion(Vector3.UnitZ, twist));

                    newNode.percentOfPath = percentOfPath;

                    pathNodes.add(newNode);

                    // calculate terms for next iteration
                    // calculate the angle for the next iteration of the loop

                    if (angle >= endAngle - 0.01)
                        done = true;
                    else
                    {
                        step += 1;
                        angle = stepSize * step;
                        if (angle > endAngle)
                            angle = endAngle;
                    }
                }
            }
        }
    }

    public String errorMessage = "";

    public ArrayList<Vector3> coords;
    public ArrayList<Vector3> normals;
    public ArrayList<Face> faces;

    public ArrayList<ViewerFace> viewerFaces;

    private int sides = 4;
    private int hollowSides = 4;
    private float profileStart = 0.0f;
    private float profileEnd = 1.0f;
    private float hollow = 0.0f;
    public int twistBegin = 0;
    public int twistEnd = 0;
    public float topShearX = 0.0f;
    public float topShearY = 0.0f;
    public float pathCutBegin = 0.0f;
    public float pathCutEnd = 1.0f;
    public float dimpleBegin = 0.0f;
    public float dimpleEnd = 1.0f;
    public float skew = 0.0f;
    public float holeSizeX = 1.0f; // called pathScaleX in pbs
    public float holeSizeY = 0.25f;
    public float taperX = 0.0f;
    public float taperY = 0.0f;
    public float radius = 0.0f;
    public float revolutions = 1.0f;
    public int stepsPerRevolution = 24;

    private int profileOuterFaceNumber = -1;
    private int profileHollowFaceNumber = -1;

    private boolean hasProfileCut = false;
    private boolean hasHollow = false;
    public boolean calcVertexNormals = false;
    private boolean normalsProcessed = false;
    public boolean viewerMode = false;
    public boolean sphereMode = false;

    public int numPrimFaces = 0;

    /// <summary>
    /// Human readable string representation of the parameters used to create a mesh.
    /// </summary>
    /// <returns></returns>
    public String ParamsToDisplayString()
    {
        String s = "sides..................: " + this.sides +
                "\nhollowSides..........: " + this.hollowSides +
                "\nprofileStart.........: " + this.profileStart +
                "\nprofileEnd...........: " + this.profileEnd +
                "\nhollow...............: " + this.hollow +
                "\ntwistBegin...........: " + this.twistBegin +
                "\ntwistEnd.............: " + this.twistEnd +
                "\ntopShearX............: " + this.topShearX +
                "\ntopShearY............: " + this.topShearY +
                "\npathCutBegin.........: " + this.pathCutBegin +
                "\npathCutEnd...........: " + this.pathCutEnd +
                "\ndimpleBegin..........: " + this.dimpleBegin +
                "\ndimpleEnd............: " + this.dimpleEnd +
                "\nskew.................: " + this.skew +
                "\nholeSizeX............: " + this.holeSizeX +
                "\nholeSizeY............: " + this.holeSizeY +
                "\ntaperX...............: " + this.taperX +
                "\ntaperY...............: " + this.taperY +
                "\nradius...............: " + this.radius +
                "\nrevolutions..........: " + this.revolutions +
                "\nstepsPerRevolution...: " + this.stepsPerRevolution +
                "\nsphereMode...........: " + this.sphereMode +
                "\nhasProfileCut........: " + this.hasProfileCut +
                "\nhasHollow............: " + this.hasHollow +
                "\nviewerMode...........: " + this.viewerMode;
        return s;
    }

    public int getProfileOuterFaceNumber()
    {
        return profileOuterFaceNumber;
    }

    public int getProfileHollowFaceNumber()
    {
        return profileHollowFaceNumber;
    }

    public boolean getHasProfileCut()
    {
        return hasProfileCut;
    }

    public boolean getHasHollow()
    {
        return hasHollow;
    }


    /**
     * Constructs a PrimMesh object and creates the profile for extrusion.
     * 
     * @param sides
     * @param profileStart
     * @param profileEnd
     * @param hollow
     * @param hollowSides
     */
    public PrimMesh(int sides, float profileStart, float profileEnd, float hollow, int hollowSides)
    {
        this.coords = new ArrayList<Vector3>();
        this.faces = new ArrayList<Face>();

        this.sides = sides;
        this.profileStart = profileStart;
        this.profileEnd = profileEnd;
        this.hollow = hollow;
        this.hollowSides = hollowSides;

        if (sides < 3)
            this.sides = 3;
        if (hollowSides < 3)
            this.hollowSides = 3;
        if (profileStart < 0.0f)
            this.profileStart = 0.0f;
        if (profileEnd > 1.0f)
            this.profileEnd = 1.0f;
        if (profileEnd < 0.02f)
            this.profileEnd = 0.02f;
        if (profileStart >= profileEnd)
            this.profileStart = profileEnd - 0.02f;
        if (hollow > 0.99f)
            this.hollow = 0.99f;
        if (hollow < 0.0f)
            this.hollow = 0.0f;
    }

    /**
     * Extrudes a profile along a path.
     * 
     * @param pathType
     */
    public void extrude(PathType pathType)
    {
        boolean needEndFaces = false;

        this.coords = new ArrayList<Vector3>();
        this.faces = new ArrayList<Face>();

        if (this.viewerMode)
        {
            this.viewerFaces = new ArrayList<ViewerFace>();
            this.calcVertexNormals = true;
        }

        if (this.calcVertexNormals)
            this.normals = new ArrayList<Vector3>();

        int steps = 1;

        float length = this.pathCutEnd - this.pathCutBegin;
        normalsProcessed = false;

        if (this.viewerMode && this.sides == 3)
        {
            // prisms don't taper well so add some vertical resolution
            // other prims may benefit from this but just do prisms for now
            if (Math.abs(this.taperX) > 0.01 || Math.abs(this.taperY) > 0.01)
                steps = (int)(steps * 4.5 * length);
        }

        if (this.sphereMode)
            this.hasProfileCut = this.profileEnd - this.profileStart < 0.4999f;
        else
            this.hasProfileCut = this.profileEnd - this.profileStart < 0.9999f;
        this.hasHollow = (this.hollow > 0.001f);

        float twistBegin = this.twistBegin / 360.0f * Helpers.TWO_PI;
        float twistEnd = this.twistEnd / 360.0f * Helpers.TWO_PI;
        float twistTotal = twistEnd - twistBegin;
        float twistTotalAbs = Math.abs(twistTotal);
        if (twistTotalAbs > 0.01f)
            steps += (int)(twistTotalAbs * 3.66); //  dahlia's magic number

        float hollow = this.hollow;

        if (pathType == PathType.Circular)
        {
            needEndFaces = false;
            if (this.pathCutBegin != 0.0f || this.pathCutEnd != 1.0f)
                needEndFaces = true;
            else if (this.taperX != 0.0f || this.taperY != 0.0f)
                needEndFaces = true;
            else if (this.skew != 0.0f)
                needEndFaces = true;
            else if (twistTotal != 0.0f)
                needEndFaces = true;
            else if (this.radius != 0.0f)
                needEndFaces = true;
        }
        else needEndFaces = true;

        // sanity checks
        float initialProfileRot = 0.0f;
        if (pathType == PathType.Circular)
        {
            if (this.sides == 3)
            {
                initialProfileRot = (float)Math.PI;
                if (this.hollowSides == 4)
                {
                    if (hollow > 0.7f)
                        hollow = 0.7f;
                    hollow *= 0.707f;
                }
                else hollow *= 0.5f;
            }
            else if (this.sides == 4)
            {
                initialProfileRot = 0.25f * (float)Math.PI;
                if (this.hollowSides != 4)
                    hollow *= 0.707f;
            }
            else if (this.sides > 4)
            {
                initialProfileRot = (float)Math.PI;
                if (this.hollowSides == 4)
                {
                    if (hollow > 0.7f)
                        hollow = 0.7f;
                    hollow /= 0.7f;
                }
            }
        }
        else
        {
            if (this.sides == 3)
            {
                if (this.hollowSides == 4)
                {
                    if (hollow > 0.7f)
                        hollow = 0.7f;
                    hollow *= 0.707f;
                }
                else hollow *= 0.5f;
            }
            else if (this.sides == 4)
            {
                initialProfileRot = 1.25f * (float)Math.PI;
                if (this.hollowSides != 4)
                    hollow *= 0.707f;
            }
            else if (this.sides == 24 && this.hollowSides == 4)
                hollow *= 1.414f;
        }

        Profile profile = new Profile(this.sides, this.profileStart, this.profileEnd, hollow, this.hollowSides, true, calcVertexNormals);
        this.errorMessage = profile.errorMessage;

        this.numPrimFaces = profile.numPrimFaces;

        int cut1FaceNumber = profile.bottomFaceNumber + 1;
        int cut2FaceNumber = cut1FaceNumber + 1;
        if (!needEndFaces)
        {
            cut1FaceNumber -= 2;
            cut2FaceNumber -= 2;
        }

        profileOuterFaceNumber = profile.outerFaceNumber;
        if (!needEndFaces)
            profileOuterFaceNumber--;

        if (hasHollow)
        {
            profileHollowFaceNumber = profile.hollowFaceNumber;
            if (!needEndFaces)
                profileHollowFaceNumber--;
        }

        int cut1Vert = -1;
        int cut2Vert = -1;
        if (hasProfileCut)
        {
            cut1Vert = hasHollow ? profile.coords.size() - 1 : 0;
            cut2Vert = hasHollow ? profile.numOuterVerts - 1 : profile.numOuterVerts;
        }

        if (initialProfileRot != 0.0f)
        {
            profile.addRot(new Quaternion(Vector3.UnitZ, initialProfileRot));
            if (viewerMode)
                profile.makeFaceUVs();
        }

        Vector3 lastCutNormal1 = new Vector3(0f);
        Vector3 lastCutNormal2 = new Vector3(0f);
        float thisV = 0.0f;
        float lastV = 0.0f;

        Path path = new Path();
        path.twistBegin = twistBegin;
        path.twistEnd = twistEnd;
        path.topShearX = topShearX;
        path.topShearY = topShearY;
        path.pathCutBegin = pathCutBegin;
        path.pathCutEnd = pathCutEnd;
        path.dimpleBegin = dimpleBegin;
        path.dimpleEnd = dimpleEnd;
        path.skew = skew;
        path.holeSizeX = holeSizeX;
        path.holeSizeY = holeSizeY;
        path.taperX = taperX;
        path.taperY = taperY;
        path.radius = radius;
        path.revolutions = revolutions;
        path.stepsPerRevolution = stepsPerRevolution;

        path.create(pathType, steps);

        for (int nodeIndex = 0; nodeIndex < path.pathNodes.size(); nodeIndex++)
        {
            PathNode node = path.pathNodes.get(nodeIndex);
            Profile newLayer = profile.copy();
            newLayer.Scale(node.xScale, node.yScale);

            newLayer.addRot(node.rotation);
            newLayer.addPos(node.position);

            if (needEndFaces && nodeIndex == 0)
            {
                newLayer.flipNormals();

                // add the bottom faces to the viewerFaces list
                if (this.viewerMode)
                {
                    Vector3 faceNormal = newLayer.faceNormal;
                    ViewerFace newViewerFace = new ViewerFace(profile.bottomFaceNumber);
                    int numFaces = newLayer.faces.size();
                    ArrayList<Face> faces = newLayer.faces;

                    for (int i = 0; i < numFaces; i++)
                    {
                        Face face = faces.get(i);
                        newViewerFace.v1 = newLayer.coords.get(face.v1);
                        newViewerFace.v2 = newLayer.coords.get(face.v2);
                        newViewerFace.v3 = newLayer.coords.get(face.v3);

                        newViewerFace.coordIndex1 = face.v1;
                        newViewerFace.coordIndex2 = face.v2;
                        newViewerFace.coordIndex3 = face.v3;

                        newViewerFace.n1 = faceNormal;
                        newViewerFace.n2 = faceNormal;
                        newViewerFace.n3 = faceNormal;

                        newViewerFace.uv1 = newLayer.faceUVs.get(face.v1);
                        newViewerFace.uv2 = newLayer.faceUVs.get(face.v2);
                        newViewerFace.uv3 = newLayer.faceUVs.get(face.v3);

                        if (pathType == PathType.Linear)
                        {
                        	/* FIXME: Need to create a copy here! */
                            newViewerFace.uv1.flip();
                            newViewerFace.uv2.flip();
                            newViewerFace.uv3.flip();
                        }

                        this.viewerFaces.add(newViewerFace);
                    }
                }
            } // if (nodeIndex == 0)

            // append this layer

            int coordsLen = this.coords.size();
            newLayer.addValue2FaceVertexIndices(coordsLen);

            this.coords.addAll(newLayer.coords);

            if (this.calcVertexNormals)
            {
                newLayer.addValue2FaceNormalIndices(this.normals.size());
                this.normals.addAll(newLayer.vertexNormals);
            }

            if (node.percentOfPath < this.pathCutBegin + 0.01f || node.percentOfPath > this.pathCutEnd - 0.01f)
                this.faces.addAll(newLayer.faces);

            // fill faces between layers

            int numVerts = newLayer.coords.size();
            Face newFace1 = new Face();
            Face newFace2 = new Face();

            thisV = 1.0f - node.percentOfPath;

            if (nodeIndex > 0)
            {
                int startVert = coordsLen + 1;
                int endVert = this.coords.size();

                if (sides < 5 || this.hasProfileCut || this.hasHollow)
                    startVert--;

                for (int i = startVert; i < endVert; i++)
                {
                    int iNext = i + 1;
                    if (i == endVert - 1)
                        iNext = startVert;

                    int whichVert = i - startVert;

                    newFace1.v1 = i;
                    newFace1.v2 = i - numVerts;
                    newFace1.v3 = iNext;

                    newFace1.n1 = newFace1.v1;
                    newFace1.n2 = newFace1.v2;
                    newFace1.n3 = newFace1.v3;
                    this.faces.add(newFace1);

                    newFace2.v1 = iNext;
                    newFace2.v2 = i - numVerts;
                    newFace2.v3 = iNext - numVerts;

                    newFace2.n1 = newFace2.v1;
                    newFace2.n2 = newFace2.v2;
                    newFace2.n3 = newFace2.v3;
                    this.faces.add(newFace2);

                    if (this.viewerMode)
                    {
                        // add the side faces to the list of viewerFaces here

                        int primFaceNum = profile.faceNumbers.get(whichVert);
                        if (!needEndFaces)
                            primFaceNum -= 1;

                        ViewerFace newViewerFace1 = new ViewerFace(primFaceNum);
                        ViewerFace newViewerFace2 = new ViewerFace(primFaceNum);

                        int uIndex = whichVert;
                        if (!hasHollow && sides > 4 && uIndex < newLayer.us.size() - 1)
                        {
                            uIndex++;
                        }

                        float u1 = newLayer.us.get(uIndex);
                        float u2 = 1.0f;
                        if (uIndex < newLayer.us.size() - 1)
                            u2 = newLayer.us.get(uIndex + 1);

                        if (whichVert == cut1Vert || whichVert == cut2Vert)
                        {
                            u1 = 0.0f;
                            u2 = 1.0f;
                        }
                        else if (sides < 5)
                        {
                            if (whichVert < profile.numOuterVerts)
                            { // boxes and prisms have one texture face per side of the prim, so the U values have to be scaled
                                // to reflect the entire texture width
                                u1 *= sides;
                                u2 *= sides;
                                u2 -= (int)u1;
                                u1 -= (int)u1;
                                if (u2 < 0.1f)
                                    u2 = 1.0f;
                            }
                        }

                        if (this.sphereMode)
                        {
                            if (whichVert != cut1Vert && whichVert != cut2Vert)
                            {
                                u1 = u1 * 2.0f - 1.0f;
                                u2 = u2 * 2.0f - 1.0f;

                                if (whichVert >= newLayer.numOuterVerts)
                                {
                                    u1 -= hollow;
                                    u2 -= hollow;
                                }

                            }
                        }

                        newViewerFace1.uv1.X = u1;
                        newViewerFace1.uv2.X = u1;
                        newViewerFace1.uv3.X = u2;

                        newViewerFace1.uv1.Y = thisV;
                        newViewerFace1.uv2.Y = lastV;
                        newViewerFace1.uv3.Y = thisV;

                        newViewerFace2.uv1.X = u2;
                        newViewerFace2.uv2.X = u1;
                        newViewerFace2.uv3.X = u2;
                     

                        newViewerFace2.uv1.Y = thisV;
                        newViewerFace2.uv2.Y = lastV;
                        newViewerFace2.uv3.Y = lastV;

                        newViewerFace1.v1 = this.coords.get(newFace1.v1);
                        newViewerFace1.v2 = this.coords.get(newFace1.v2);
                        newViewerFace1.v3 = this.coords.get(newFace1.v3);

                        newViewerFace2.v1 = this.coords.get(newFace2.v1);
                        newViewerFace2.v2 = this.coords.get(newFace2.v2);
                        newViewerFace2.v3 = this.coords.get(newFace2.v3);

                        newViewerFace1.coordIndex1 = newFace1.v1;
                        newViewerFace1.coordIndex2 = newFace1.v2;
                        newViewerFace1.coordIndex3 = newFace1.v3;

                        newViewerFace2.coordIndex1 = newFace2.v1;
                        newViewerFace2.coordIndex2 = newFace2.v2;
                        newViewerFace2.coordIndex3 = newFace2.v3;

                        // profile cut faces
                        if (whichVert == cut1Vert)
                        {
                            newViewerFace1.primFaceNumber = cut1FaceNumber;
                            newViewerFace2.primFaceNumber = cut1FaceNumber;
                            newViewerFace1.n1 = newLayer.cutNormal1;
                            newViewerFace1.n2 = newViewerFace1.n3 = lastCutNormal1;

                            newViewerFace2.n1 = newViewerFace2.n3 = newLayer.cutNormal1;
                            newViewerFace2.n2 = lastCutNormal1;
                        }
                        else if (whichVert == cut2Vert)
                        {
                            newViewerFace1.primFaceNumber = cut2FaceNumber;
                            newViewerFace2.primFaceNumber = cut2FaceNumber;
                            newViewerFace1.n1 = newLayer.cutNormal2;
                            newViewerFace1.n2 = lastCutNormal2;
                            newViewerFace1.n3 = lastCutNormal2;

                            newViewerFace2.n1 = newLayer.cutNormal2;
                            newViewerFace2.n3 = newLayer.cutNormal2;
                            newViewerFace2.n2 = lastCutNormal2;
                        }

                        else // outer and hollow faces
                        {
                            if ((sides < 5 && whichVert < newLayer.numOuterVerts) || (hollowSides < 5 && whichVert >= newLayer.numOuterVerts))
                            { // looks terrible when path is twisted... need vertex normals here
                                newViewerFace1.calcSurfaceNormal();
                                newViewerFace2.calcSurfaceNormal();
                            }
                            else
                            {
                                newViewerFace1.n1 = this.normals.get(newFace1.n1);
                                newViewerFace1.n2 = this.normals.get(newFace1.n2);
                                newViewerFace1.n3 = this.normals.get(newFace1.n3);

                                newViewerFace2.n1 = this.normals.get(newFace2.n1);
                                newViewerFace2.n2 = this.normals.get(newFace2.n2);
                                newViewerFace2.n3 = this.normals.get(newFace2.n3);
                            }
                        }

                        this.viewerFaces.add(newViewerFace1);
                        this.viewerFaces.add(newViewerFace2);

                    }
                }
            }

            lastCutNormal1 = newLayer.cutNormal1;
            lastCutNormal2 = newLayer.cutNormal2;
            lastV = thisV;

            if (needEndFaces && nodeIndex == path.pathNodes.size() - 1 && viewerMode)
            {
                // add the top faces to the viewerFaces list here
                Vector3 faceNormal = newLayer.faceNormal;
                ViewerFace newViewerFace = new ViewerFace(0);
                int numFaces = newLayer.faces.size();
                ArrayList<Face> faces = newLayer.faces;

                for (int i = 0; i < numFaces; i++)
                {
                    Face face = faces.get(i);
                    newViewerFace.v1 = newLayer.coords.get(face.v1 - coordsLen);
                    newViewerFace.v2 = newLayer.coords.get(face.v2 - coordsLen);
                    newViewerFace.v3 = newLayer.coords.get(face.v3 - coordsLen);

                    newViewerFace.coordIndex1 = face.v1 - coordsLen;
                    newViewerFace.coordIndex2 = face.v2 - coordsLen;
                    newViewerFace.coordIndex3 = face.v3 - coordsLen;

                    newViewerFace.n1 = faceNormal;
                    newViewerFace.n2 = faceNormal;
                    newViewerFace.n3 = faceNormal;

                    newViewerFace.uv1 = newLayer.faceUVs.get(face.v1 - coordsLen);
                    newViewerFace.uv2 = newLayer.faceUVs.get(face.v2 - coordsLen);
                    newViewerFace.uv3 = newLayer.faceUVs.get(face.v3 - coordsLen);

                    if (pathType == PathType.Linear)
                    {
                        newViewerFace.uv1.flip();
                        newViewerFace.uv2.flip();
                        newViewerFace.uv3.flip();
                    }

                    this.viewerFaces.add(newViewerFace);
                }
            }
        } // for (int nodeIndex = 0; nodeIndex < path.pathNodes.Count; nodeIndex++)
    }

    /**
     * DEPRICATED - use Extrude(PathType.Linear) instead
     * 
     * Extrudes a profile along a straight line path. Used for prim types box, cylinder, and prism.
     */ 
	@Deprecated
    public void extrudeLinear()
    {
        this.extrude(PathType.Linear);
    }


    /**
     * DEPRICATED - use Extrude(PathType.Circular) instead
     *
     * Extrude a profile into a circular path prim mesh. Used for prim types torus, tube, and ring.
     *
     */
	@Deprecated
    public void extrudeCircular()
    {
        this.extrude(PathType.Circular);
    }


    private Vector3 surfaceNormal(Vector3 c1, Vector3 c2, Vector3 c3)
    {
    	Vector3 edge1 = Vector3.subtract(c1, c2);
    	Vector3 edge2 = Vector3.subtract(c3, c1);

    	return edge1.cross(edge2).normalize();
    }

    private Vector3 surfaceNormal(Face face)
    {
        return surfaceNormal(this.coords.get(face.v1), this.coords.get(face.v2), this.coords.get(face.v3));
    }

    /**
     * Calculate the surface normal for a face in the list of faces
     * 
     * @param faceIndex
     * @throws Exception 
     * @returns
     */
    public Vector3 surfaceNormal(int faceIndex) throws Exception
    {
        int numFaces = this.faces.size();
        if (faceIndex < 0 || faceIndex >= numFaces)
            throw new Exception("faceIndex out of range");

        return surfaceNormal(this.faces.get(faceIndex));
    }

    /**
     * Duplicates a PrimMesh object. All object properties are copied by value, including lists.
     * 
     * @returns
     */
    public PrimMesh copy()
    {
        PrimMesh copy = new PrimMesh(this.sides, this.profileStart, this.profileEnd, this.hollow, this.hollowSides);
        copy.twistBegin = this.twistBegin;
        copy.twistEnd = this.twistEnd;
        copy.topShearX = this.topShearX;
        copy.topShearY = this.topShearY;
        copy.pathCutBegin = this.pathCutBegin;
        copy.pathCutEnd = this.pathCutEnd;
        copy.dimpleBegin = this.dimpleBegin;
        copy.dimpleEnd = this.dimpleEnd;
        copy.skew = this.skew;
        copy.holeSizeX = this.holeSizeX;
        copy.holeSizeY = this.holeSizeY;
        copy.taperX = this.taperX;
        copy.taperY = this.taperY;
        copy.radius = this.radius;
        copy.revolutions = this.revolutions;
        copy.stepsPerRevolution = this.stepsPerRevolution;
        copy.calcVertexNormals = this.calcVertexNormals;
        copy.normalsProcessed = this.normalsProcessed;
        copy.viewerMode = this.viewerMode;
        copy.numPrimFaces = this.numPrimFaces;
        copy.errorMessage = this.errorMessage;

        copy.coords = new ArrayList<Vector3>(this.coords);
        copy.faces = new ArrayList<Face>(this.faces);
        copy.viewerFaces = new ArrayList<ViewerFace>(this.viewerFaces);
        copy.normals = new ArrayList<Vector3>(this.normals);

        return copy;
    }

    /**
     * Calculate surface normals for all of the faces in the list of faces in this mesh
     * @throws Exception 
     */
    public void calcNormals() throws Exception
    {
        if (normalsProcessed)
            return;

        normalsProcessed = true;

        int numFaces = faces.size();

        if (!this.calcVertexNormals)
            this.normals = new ArrayList<Vector3>();

        for (int i = 0; i < numFaces; i++)
        {
            Face face = faces.get(i);

            this.normals.add(surfaceNormal(i));

            int normIndex = normals.size() - 1;
            face.n1 = normIndex;
            face.n2 = normIndex;
            face.n3 = normIndex;

            this.faces.set(i, face);
        }
    }

    /**
     * Adds a value to each XYZ vertex coordinate in the mesh
     * 
     * @param x
     * @param y
     * @param z
     */
    public void addPos(float x, float y, float z)
    {
        int i;
        int numVerts = this.coords.size();
        Vector3 vert;

        for (i = 0; i < numVerts; i++)
        {
            vert = this.coords.get(i);
            vert.X += x;
            vert.Y += y;
            vert.Z += z;
            this.coords.set(i, vert);
        }

        if (this.viewerFaces != null)
        {
            int numViewerFaces = this.viewerFaces.size();

            for (i = 0; i < numViewerFaces; i++)
            {
                ViewerFace v = this.viewerFaces.get(i);
                v.addPos(x, y, z);
                this.viewerFaces.set(i, v);
            }
        }
    }

    /**
     * Rotates the mesh
     *
     * @param q
     */
    public void addRot(Quaternion quaternion)
    {
        int i;
        int numVerts = this.coords.size();

        for (i = 0; i < numVerts; i++)
        	this.coords.set(i, Vector3.multiply(this.coords.get(i), quaternion));

        if (this.normals != null)
        {
            int numNormals = this.normals.size();
            for (i = 0; i < numNormals; i++)
            	this.normals.set(i, Vector3.multiply(this.normals.get(i), quaternion));
        }

        if (this.viewerFaces != null)
        {
            int numViewerFaces = this.viewerFaces.size();

            for (i = 0; i < numViewerFaces; i++)
            {
                ViewerFace v = this.viewerFaces.get(i);
                v.v1 = Vector3.multiply(v.v1, quaternion);
                v.v2 = Vector3.multiply(v.v2, quaternion);
                v.v3 = Vector3.multiply(v.v3, quaternion);
                
                v.n1 = Vector3.multiply(v.n1, quaternion);
                v.n2 = Vector3.multiply(v.n2, quaternion);
                v.n3 = Vector3.multiply(v.n3, quaternion);
                this.viewerFaces.set(i, v);
            }
        }
    }

    public VertexIndexer getVertexIndexer()
    {
        if (this.viewerMode && this.viewerFaces.size() > 0)
            return new VertexIndexer(this);
        return null;
    }

    /**
     * Scales the mesh
     *
     * @param x
     * @param y
     * @param z
     */
    public void scale(float x, float y, float z)
    {
        int i;
        int numVerts = this.coords.size();

        Vector3 m = new Vector3(x, y, z);
        for (i = 0; i < numVerts; i++)
            this.coords.set(i, Vector3.multiply(this.coords.get(i), m));

        if (this.viewerFaces != null)
        {
            int numViewerFaces = this.viewerFaces.size();
            for (i = 0; i < numViewerFaces; i++)
            {
                ViewerFace v = this.viewerFaces.get(i);
                v.v1 = Vector3.multiply(v.v1, m);
                v.v2 = Vector3.multiply(v.v2, m);
                v.v3 = Vector3.multiply(v.v3, m);
                this.viewerFaces.set(i, v);
            }
        }
    }

    /**
     * Dumps the mesh to a Blender compatible "Raw" format file
     *
     * @param path
     * @param name
     * @param title
     * @throws IOException 
     */
    public void dumpRaw(String path, String name, String title) throws IOException
    {
        if (path == null)
            return;
        String fileName = name + "_" + title + ".raw";
        File completePath = new File(path, fileName);
        FileWriter sw = new FileWriter(completePath);

        for (int i = 0; i < this.faces.size(); i++)
        {
        	sw.append(this.coords.get(this.faces.get(i).v1).toString() + " ");
        	sw.append(this.coords.get(this.faces.get(i).v2).toString() + " ");
        	sw.append(this.coords.get(this.faces.get(i).v3).toString() + "\n");
        }
        sw.close();
    }
}