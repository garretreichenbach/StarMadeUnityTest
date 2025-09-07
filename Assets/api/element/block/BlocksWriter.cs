using System;
using System.Collections.Generic;
using System.IO;
using System.Text;

namespace Api.Element.Block
{
    /// <summary>
    /// Used to generate the <see cref="Blocks"/> enum that contains all the block data.
    /// <br/>Make sure to run this every time the block properties file is updated.
    /// </summary>
    public class BlocksWriter
    {
        private const string BlockProperties = "BlockTypes.properties";
        private const string BlockConfig = "BlockConfig.xml";
        private const string BlocksEnum = "Blocks.cs";

        private static readonly Dictionary<short, string> blockMap = new Dictionary<short, string>();
        private static readonly Dictionary<string, string> replacements = new Dictionary<string, string>();

        public static void GenerateBlocksEnum()
        {
            ReorderProperties();
            ImportProperties();
            UpdateBlockConfig();
        }

        /// <summary>
        /// Reorders BlockTypes.properties so that the blocks are in order of their ID.
        /// </summary>
        private static void ReorderProperties()
        {
            try
            {
                //Reorder the block properties file so that the blocks are in order of their ID
                using (StreamReader reader = new StreamReader(BlockProperties, Encoding.UTF8))
                {
                    string line;
                    while ((line = reader.ReadLine()) != null)
                    {
                        //Skip comments
                        if (line.StartsWith("#"))
                        {
                            if (line.StartsWith("#UNUSED"))
                            {
                                string[] split = line.Split('=');
                                if (split.Length < 2) continue;
                                short id = short.Parse(split[1]);
                                blockMap.Add(id, null);
                            }
                        }
                        else
                        {
                            //Split the line into the block name and ID
                            string[] split = line.Split('=');
                            if (split.Length < 2) continue;
                            string block = split[0];
                            short id = short.Parse(split[1]);
                            if (block.Contains("-"))
                            {
                                //Some blocks have a - in their name like DECORATIVE-COMPUTER, we need to replace it with an underscore
                                replacements.Add(block, block.Replace("-", "_"));
                                block = block.Replace("-", "_");
                            }
                            if (block.Contains("(") || block.Contains(")"))
                            {
                                //Some blocks have parenthesis in their name like DECORATIVE_COMPUTER_(ORANGE), we need to remove the parentheses
                                replacements.Add(block, block.Replace("(", "").Replace(")", ""));
                                block = block.Replace("(", "").Replace(")", "");
                            }
                            if (block.Contains("/"))
                            {
                                //Some blocks have a / in their name for things like slabs, we need to replace it with something else
                                //For example 1/2 becomes half, 1/4 becomes quarter, etc.
                                if (block.Contains("1/2"))
                                {
                                    replacements.Add(block, block.Replace("1/2", "HALF_SLAB"));
                                    block = block.Replace("1/2", "HALF_SLAB");
                                }
                                if (block.Contains("1/4"))
                                {
                                    replacements.Add(block, block.Replace("1/4", "QUARTER_SLAB"));
                                    block = block.Replace("1/4", "QUARTER_SLAB");
                                }
                                if (block.Contains("3/4"))
                                {
                                    replacements.Add(block, block.Replace("3/4", "THREE_QUARTER_SLAB"));
                                    block = block.Replace("3/4", "THREE_QUARTER_SLAB");
                                }
                            }
                            blockMap.Add(id, block);
                        }
                    }
                }

                //Write the reordered block properties file
                using (StreamWriter writer = new StreamWriter(BlockProperties, false, Encoding.UTF8))
                {
                    foreach (KeyValuePair<short, string> entry in blockMap)
                    {
                        if (entry.Value == null)
                        {
                            writer.WriteLine("#UNUSED=" + entry.Key);
                        }
                        else
                        {
                            writer.WriteLine(entry.Value + "=" + entry.Key);
                        }
                    }
                }
            }
            catch (Exception exception)
            {
                Console.WriteLine(exception);
            }
        }

        /// <summary>
        /// Imports the block properties from BlockTypes.properties and writes them to the Blocks enum.
        /// </summary>
        private static void ImportProperties()
        {
            try
            {
                using (StreamReader reader = new StreamReader(BlockProperties, Encoding.UTF8))
                using (StreamWriter writer = new StreamWriter(BlocksEnum, false, Encoding.UTF8))
                {
                    if (File.Exists(BlocksEnum)) File.Delete(BlocksEnum);
                    File.Create(BlocksEnum).Close();

                    // Read the block properties file
                    Dictionary<short, string> blockMap = new Dictionary<short, string>();
                    string line;
                    blockMap.Add(0, "EMPTY_SPACE");
                    while ((line = reader.ReadLine()) != null)
                    {
                        //Skip comments
                        if (line.StartsWith("#")) continue;
                        //Split the line into the block name and ID
                        string[] split = line.Split('=');
                        if (split.Length < 2) continue;
                        string block = split[0];
                        short id = short.Parse(split[1]);
                        if (block.Contains("-"))
                        {
                            //Some blocks have a - in their name like DECORATIVE-COMPUTER, we need to replace it with an underscore
                            replacements.Add(block, block.Replace("-", "_"));
                            block = block.Replace("-", "_");
                        }
                        if (block.Contains("(") || block.Contains(")"))
                        {
                            //Some blocks have parenthesis in their name like DECORATIVE_COMPUTER_(ORANGE), we need to remove the parentheses
                            replacements.Add(block, block.Replace("(", "").Replace(")", ""));
                            block = block.Replace("(", "").Replace(")", "");
                        }
                        if (block.Contains("/"))
                        {
                            //Some blocks have a / in their name for things like slabs, we need to replace it with something else
                            //For example 1/2 becomes half, 1/4 becomes quarter, etc.
                            if (block.Contains("1/2"))
                            {
                                replacements.Add(block, block.Replace("1/2", "HALF_SLAB"));
                                block = block.Replace("1/2", "HALF_SLAB");
                            }
                            if (block.Contains("1/4"))
                            {
                                replacements.Add(block, block.Replace("1/4", "QUARTER_SLAB"));
                                block = block.Replace("1/4", "QUARTER_SLAB");
                            }
                            if (block.Contains("3/4"))
                            {
                                replacements.Add(block, block.Replace("3/4", "THREE_QUARTER_SLAB"));
                                block = block.Replace("3/4", "THREE_QUARTER_SLAB");
                            }
                        }
                        blockMap.Add(id, block);
                    }
                    //Write the start of the enum to the file
                    writer.WriteLine("using System;\nusing System.Collections.Generic;\n\nnamespace Api.Element.Block\n{\n    /// <summary>\n    /// Auto generated by BlocksWriter, don't edit this file manually.\n    /// </summary>\n    public enum Blocks\n    {");
                    //Write the block data to the file
                    int i = 0;
                    foreach (KeyValuePair<short, string> entry in blockMap)
                    {
                        short id = entry.Key;
                        string block = entry.Value;
                        if (i == blockMap.Count - 1)
                        {
                            writer.WriteLine("\t" + block + " = " + id);
                        }
                        else
                        {
                            writer.WriteLine("\t" + block + " = " + id + ",");
                        }
                        i++;
                    }
                    writer.WriteLine("    }\n}");
                }
            }
            catch (IOException exception)
            {
                Console.WriteLine(exception);
            }
        }

        private static void UpdateBlockConfig()
        {
            StringBuilder writer = new StringBuilder();
            try
            {
                using (StreamReader reader = new StreamReader(BlockConfig, Encoding.UTF8))
                {
                    string line;
                    while ((line = reader.ReadLine()) != null)
                    {
                        foreach (KeyValuePair<string, string> entry in replacements)
                        {
                            line = line.Replace(entry.Key, entry.Value);
                        }
                        writer.AppendLine(line);
                    }
                }
            }
            catch (IOException exception)
            {
                Console.WriteLine(exception);
            }

            try
            {
                using (StreamWriter bufferedWriter = new StreamWriter(BlockConfig, false, Encoding.UTF8))
                {
                    bufferedWriter.Write(writer.ToString());
                }
            }
            catch (IOException exception)
            {
                Console.WriteLine(exception);
            }
        }
    }
}
