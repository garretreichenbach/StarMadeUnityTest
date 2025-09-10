using UnityEngine;
using Universe.Data.Chunk;

namespace Dev {
    public class PlayerControls : MonoBehaviour {
        void Update() {
            if (Input.GetMouseButtonDown(0) || Input.GetMouseButtonDown(1)) {
                var ray = Camera.main.ScreenPointToRay(Input.mousePosition);
                if (Physics.Raycast(ray, out var hit)) {
                    var chunk = hit.collider.GetComponent<Chunk>();
                    if (chunk != null) {
                        var pos = hit.point;
                        if (Input.GetMouseButtonDown(0)) {
                            pos -= ray.direction * 0.01f;
                        } else {
                            pos += ray.direction * 0.01f;
                        }

                        var blockX = (int) (pos.x - chunk.transform.position.x);
                        var blockY = (int) (pos.y - chunk.transform.position.y);
                        var blockZ = (int) (pos.z - chunk.transform.position.z);

                        var blockIndex = chunk.Data.GetBlockIndex(new Vector3(blockX, blockY, blockZ));

                        if (Input.GetMouseButtonDown(0)) {
                            chunk.Data.SetBlockType(blockIndex, 0);
                        } else {
                            chunk.Data.SetBlockType(blockIndex, 1);
                        }

                        chunk.Rebuild();
                    }
                }
            }
        }
    }
}