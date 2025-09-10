using UnityEngine;
using Universe.Data.Chunk;
using Universe.Data.GameEntity;

namespace Dev {
    public class PlayerControls : MonoBehaviour {
        void Update() {
            if (Input.GetMouseButtonDown(0) || Input.GetMouseButtonDown(1)) {
                var ray = Camera.main.ScreenPointToRay(Input.mousePosition);
                if (Physics.Raycast(ray, out var hit)) {
                    var entity = hit.collider.GetComponent<GameEntity>();
                    if (entity != null) {
                        var chunk = entity.GetChunk();
                        var pos = hit.point;
                        if (Input.GetMouseButtonDown(0)) {
                            pos -= ray.direction * 0.01f;
                        } else {
                            pos += ray.direction * 0.01f;
                        }

                        var blockX = (int) (pos.x - entity.transform.position.x);
                        var blockY = (int) (pos.y - entity.transform.position.y);
                        var blockZ = (int) (pos.z - entity.transform.position.z);

                        var blockIndex = chunk.Data.GetBlockIndex(new Vector3(blockX, blockY, blockZ));

                        if (Input.GetMouseButtonDown(0)) {
                            chunk.Data.SetBlockType(blockIndex, 0);
                        } else {
                            chunk.Data.SetBlockType(blockIndex, 1);
                        }

                        entity.RebuildChunkMeshes();
                    }
                }
            }
        }
    }
}