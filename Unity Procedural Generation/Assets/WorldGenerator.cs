using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class WorldGenerator : MonoBehaviour
{
    public Terrain terrain;
    public int width;
    public int height;
    public float scale;
    public float startX;
    public float startY;
    public float heightScale; // can actually get higher than maxHeight...

    void Update()
    {
        // GenerateMap();
        SetTerrainHeights(GenerateHeightMap());
    }

    void GenerateMap()
    {
        float[,] noiseMap = GenerateHeightMap();
    }

    static float[,] GenerateNoiseMap(int width, int height, float scale, float startX, float startY)
    {
        float[,] noiseMap = new float[width, height];

        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                noiseMap[x, y] = Mathf.PerlinNoise(scale * x / width + startX, scale * y / height + startY);
            }
        }

        return noiseMap;
    }

    public float powAmount;

    float[,] GenerateHeightMap()
    {
        float[,] heightMap = GenerateNoiseMap(width, height, scale, startX, startY);

        for (int r = 0; r < heightMap.GetLength(0); r++)
        {
            for (int c = 0; c < heightMap.GetLength(1); c++)
            {
                float noiseValue = heightMap[r, c];
                float heightValue = Mathf.Pow(noiseValue, powAmount);
                heightMap[r, c] = Mathf.Lerp(0f, heightScale, heightValue);
            }
        }

        return heightMap;
    }

    static Texture2D GenerateColoredTexture2D(float[,] valueMap)
    {
        int width = valueMap.GetLength(0);
        int height = valueMap.GetLength(1);

        Texture2D texture = new Texture2D(width, height);

        Color[] colorMap = new Color[width * height];
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                colorMap[y * width + x] = Color.Lerp(Color.white, Color.black, valueMap[x, y]);
            }
        }
        texture.SetPixels(colorMap);
        texture.Apply();
        return texture;
    }

    void SetTerrainHeights(float[,] heightMap)
    {
        TerrainData terrainData = terrain.terrainData;
        Vector3 terrainSize = terrainData.size;
        terrainData.heightmapResolution = heightMap.GetLength(0) + 1; // Adjust the heightmap resolution
        terrainData.size = terrainSize; // because setting resolution changes size
        terrainData.SetHeights(0, 0, heightMap);
    }
}
