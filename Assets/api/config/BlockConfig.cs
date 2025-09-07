using System;
using System.Collections.Generic;
using System.Reflection;
using UnityEngine;

//TODO: These are placeholder classes. They should be replaced with the actual classes once they are converted.

namespace Api.Config
{
    public class BlockConfig
    {
        public enum RailType { Track, Rotator, Turret }

        public static void AddCustomRail(ElementInformation info, RailType railType)
        {
            if (railType.Equals(RailType.Track) || railType.Equals(RailType.Rotator))
            {
                info.setBlockStyle(BlockStyle.NORMAL24);
            }
            info.railType = railType;
        }

        /// <summary>
        /// DO NOT USE THIS FOR MODDED ELEMENTS, config.add(info) will automatically do this for you
        /// For new elements, this is not needed, however for vanilla items, they have already been inserted into the element hierarchy, so it needs to be re-added
        /// </summary>
        /// <param name="info"></param>
        /// <param name="category"></param>
        public static void SetElementCategory(ElementInformation info, ElementCategory category)
        {
            ElementKeyMap.getCategoryHirarchy().RemoveRecursive(info);
            info.type = category;
            ElementKeyMap.getCategoryHirarchy().InsertRecusrive(info);
        }

        /// <summary>
        /// Creates a new item category. TOP
        /// </summary>
        public static ElementCategory NewElementCategory(ElementCategory top, string name)
        {
            ElementCategory newCat = new ElementCategory(name, top);
            top.getChildren().Add(newCat);
            return newCat;
        }

        /// <summary>
        /// Removes a category from the element hierarchy. If the category has children or elements, it will not be removed.
        /// </summary>
        /// <param name="category">The category to remove</param>
        /// <returns>true if the category was removed, false if it was not</returns>
        public static bool RemoveCategory(ElementCategory category)
        {
            if (!category.hasChildren() && category.getInfoElements().Count == 0)
            {
                foreach (ElementCategory cat in GetChildCategoriesRecursive(ElementKeyMap.getCategoryHirarchy()))
                {
                    if (cat.getChildren().Contains(category))
                    {
                        cat.getChildren().Remove(category);
                        return true;
                    }
                }
            }
            return false;
        }

        public static List<ElementCategory> GetChildCategoriesRecursive(ElementCategory category)
        {
            List<ElementCategory> categories = new List<ElementCategory>();
            categories.Add(category);
            foreach (ElementCategory cat in category.getChildren()) categories.AddRange(GetChildCategoriesRecursive(cat));
            return categories;
        }

        public static void AssignLod(ElementInformation info, StarMod mod, string lodName, string activationLod)
        {
            info.blended = true;
            info.individualSides = 6;
            info.blockStyle = BlockStyle.NORMAL24;
            info.physical = true;
            //Needs to be true for lod blocks for whatever reason
            info.drawOnlyInBuildMode = true;
            info.lodActivationAnimationStyle = 1;
            if (activationLod != null) info.lodShapeStringActive = mod.getName() + "~" + activationLod;
            else info.lodShapeStringActive = mod.getName() + "~" + lodName;
            info.lodShapeString = mod.getName() + "~" + lodName;
            info.placable = true;
            info.lodCollisionPhysical = true;
            info.cubeCubeCollision = true;
            info.blockResourceType = 2;
            info.blockStyle = BlockStyle.SPRITE;
        }

        /// <summary>
        ///
        /// </summary>
        /// <param name="factoryType">[0=None, 1=Capsule Refinery, 2=Micro Assembler, 3=Basic Factory, 4=Standard Factory, 5=Advanced Factory, 6+ = Custom Factory]</param>
        public static void AddRecipe(ElementInformation info, int factoryType, int bakeTime, params FactoryResource[] resources)
        {
            info.consistence.Clear();
            info.cubatomConsistence.Clear();
            foreach (FactoryResource resource in resources)
            {
                info.consistence.Add(resource);
                info.cubatomConsistence.Add(resource);
            }
            info.factoryBakeTime = bakeTime;
            info.producedInFactory = factoryType;
        }

        /// <summary>
        ///
        /// </summary>
        /// <param name="recipeArray">The recipe array(FixedRecipe is a list of recipes) that you want to put the recipe in,
        ///                   Example of a valid FixedRecipe: ElementKeyMap.capsuleRecipe</param>
        public static void AddRefineryRecipe(FixedRecipe recipeArray, FactoryResource[] input, FactoryResource[] output)
        {
            //my man schema really hates arraylists
            FixedRecipeProduct[] array = new FixedRecipeProduct[recipeArray.recipeProducts.Length + 1];
            Array.Copy(recipeArray.recipeProducts, array, recipeArray.recipeProducts.Length);
            
            FixedRecipeProduct product = new FixedRecipeProduct();
            product.input = input;
            product.output = output;
            array[array.Length - 1] = product;
            recipeArray.recipeProducts = array;
        }
        public static void RegisterComputerModulePair(short computer, short module)
        {
            ElementInformation comp = ElementKeyMap.infoArray[computer];
            comp.mainCombinationController = true;
            comp.systemBlock = true;
            comp.controlledBy.Add(1);
            comp.controlling.Add(module);

            ElementKeyMap.infoArray[module].controlledBy.Add(computer);
        }

        public static void SetBlocksConnectable(ElementInformation master, ElementInformation slave)
        {
            master.controlling.Add(slave.id);
            slave.controlledBy.Add(master.id);
        }

        public static void ClearRecipes(ElementInformation element)
        {
            element.cubatomConsistence.Clear();
            element.consistence.Clear();
        }

        /// <summary>
        /// Name/descriptions are controlled by these localization hashmaps.
        /// You will need to update or remove them to set their name
        /// </summary>
        public static void ResetLocalization(ElementInformation info)
        {
            ElementKeyMap.nameTranslations.Remove(info.getId());
            ElementKeyMap.descriptionTranslations.Remove(info.getId());
        }

        public static void SetBasicInfo(ElementInformation info, string description, int price, float mass, bool placeable, bool activatable, int buildIcoNum)
        {
            SetBasicInfo(info, description, price, mass, placeable, activatable, buildIcoNum, 1, 64);
        }

        public static void SetBasicInfo(ElementInformation info, string description, int price, float mass, bool placeable, bool activatable, int buildIcoNum, float volume, int blockHp)
        {
            info.description = description;
            info.price = price;
            info.blockStyle = BlockStyle.NORMAL;
            info.setOrientatable(true);
            info.mass = mass;
            info.placable = placeable;
            info.volume = volume;
            info.maxHitPointsFull = blockHp;
            info.setCanActivate(activatable);
            info.setBuildIconNum(buildIcoNum);
        }
        private static readonly HashSet<short> factoryStandardControlledBy = new HashSet<short>();
        private static readonly HashSet<short> factoryStandardControlling = new HashSet<short>();
        public static void ClearData()
        {
            factoryStandardControlling.Clear();
            factoryStandardControlledBy.Clear();
            CustomFactories.Clear();
            CustomFactoryIds.Clear();
            RestrictedBlocks.Clear();
            CustomModRefineries.Clear();
            customFactoryIdLog = 20;
            addedOres = 0;

            factoryStandardControlledBy.Add(ElementKeyMap.STASH_ELEMENT);
            factoryStandardControlledBy.Add(Blocks.LOCK_BOX.getId());

            factoryStandardControlling.Add(ElementKeyMap.CARGO_SPACE);
            factoryStandardControlling.Add(ElementKeyMap.FACTORY_INPUT_ENH_ID);//Factory enhancer
        }

        public readonly static Dictionary<short, int> CustomFactories = new Dictionary<short, int>();
        public readonly static Dictionary<int, short> CustomFactoryIds = new Dictionary<int, short>();

        // Blocks that can only have 1 of them placed
        public readonly static List<short> RestrictedBlocks = new List<short>();
        public readonly static Dictionary<short, CustomModRefinery> CustomModRefineries = new Dictionary<short, CustomModRefinery>();


        public static bool IsCustomModRefinery(short blockId)
        {
            return CustomModRefineries.ContainsKey(blockId);
        }
        private static int customFactoryIdLog = 20;
        private static int addedOres = 0;
        public static int GetAddedOres()
        {
            return addedOres;
        }

        public static bool IsCustomModExtractor(short type)
        {
            throw new NotImplementedException();
        }

        public static ElementInformation NewRefinery(StarMod mod, string name, short[] ids, CustomModRefinery modRefinery)
        {
            ElementInformation elem = NewElement(mod, name, ids);
            elem.buildIconNum = 231;
            elem.blockResourceType = 4;
            elem.basicResourceFactory = 0;
            elem.factory = new BlockFactory();
            elem.factory.enhancer = ElementKeyMap.FACTORY_INPUT_ENH_ID;
            elem.canActivate = true;
            elem.drawLogicConnection = true;

            elem.controlledBy.UnionWith(factoryStandardControlledBy);
            elem.controlling.UnionWith(factoryStandardControlling);
            foreach (short id in elem.controlling)
            {
                ElementInformation e = ElementKeyMap.getInfo(id);
                e.controlledBy.Add(elem.id);
            }

            factoryStandardControlledBy.Add(elem.id);
            factoryStandardControlling.Add(elem.id);

            CustomModRefineries.Add(elem.id, modRefinery);
            return elem;
        }

        public static ElementInformation NewFactory(StarMod mod, string name, short[] ids)
        {
            ElementInformation elem = NewElement(mod, name, ids);
            elem.buildIconNum = 231;
            elem.blockResourceType = 4;
            elem.basicResourceFactory = 0;
            elem.factory = new BlockFactory();
            elem.factory.enhancer = ElementKeyMap.FACTORY_INPUT_ENH_ID;
            elem.canActivate = true;
            elem.drawLogicConnection = true;

            elem.controlledBy.UnionWith(factoryStandardControlledBy);
            elem.controlling.UnionWith(factoryStandardControlling);
            foreach (short id in elem.controlling)
            {
                ElementInformation e = ElementKeyMap.getInfo(id);
                e.controlledBy.Add(elem.id);
            }

            factoryStandardControlledBy.Add(elem.id);
            factoryStandardControlling.Add(elem.id);

            CustomFactoryIds.Add(customFactoryIdLog, elem.id);
            CustomFactories.Add(elem.id, customFactoryIdLog++);
            return elem;
        }

        public static Tuple<ElementInformation, int> NewOre(StarMod mod, string name, int buildIcon, object image)
        {
            addedOres++;
            ElementInformation block = NewElement(mod, name, new short[] { 186 });

            //Ores are generally not placeable
            block.resourceInjection = ElementInformation.ResourceInjectionType.ORE;
            block.blockResourceType = 0;

            block.buildIconNum = buildIcon;
            block.inRecipe = true;
            block.individualSides = 1;
            block.blockStyle = BlockStyle.SPRITE;
            int resourceId = (int)UniversalRegistry.getExistingURV(UniversalRegistry.RegistryType.ORE, mod, name);

            //Bind a custom ore texture to the URV id.
            StarLoaderTexture starLoaderTexture = StarLoaderTexture.newOverlayTexture(image, resourceId);

            if (block.id > 2047)
            {
                throw new Exception("Ok so basically, the resIDToOrientationMapping array is too small. If you're seeing this ask me to convert it to a bimap");
            }
            if (resourceId > 255)
            {
                throw new Exception("You've allocated to many ores. If this ever happens, ask me to convert it to a bimap");
            }
            Debug.LogError("[StarLoader] Registering mod ore: " + resourceId + ", Name: " + block.getName());
            ElementKeyMap.resources[resourceId - 1] = block.id;
            ElementKeyMap.orientationToResIDMapping[resourceId] = block.id;
            ElementKeyMap.resIDToOrientationMapping[block.id] = (byte)resourceId;
            ElementKeyMap.orientationToResOverlayMapping[resourceId] = (byte)resourceId + 1;
            return new Tuple<ElementInformation, int>(block, resourceId);
        }

        private static short GetIdOfName(string namespacedName)
        {
            if (ElementKeyMap.properties.ContainsKey(namespacedName))
            {
                // Block exists in BlockTypes.properties

                // The ID of the element in BlockTypes.properties
                short blockId = (short)int.Parse(ElementKeyMap.properties[namespacedName].ToString());
                UniversalRegistry.registerCustomURV(UniversalRegistry.RegistryType.BLOCK_ID, ModPlayground.inst.getSkeleton(), namespacedName, blockId);
                return blockId;
            }
            else
            {
                // Block does not exist in properties
                long? resourceId = UniversalRegistry.getExistingURVOrNull(UniversalRegistry.RegistryType.BLOCK_ID, ModPlayground.inst, namespacedName);
                if (resourceId == null)
                {
                    // Block is actually new, insert into properties and put the id into the UR
                    int blockId = ElementKeyMap.insertIntoProperties(namespacedName);
                    UniversalRegistry.registerCustomURV(UniversalRegistry.RegistryType.BLOCK_ID, ModPlayground.inst.getSkeleton(), namespacedName, blockId);
                    Debug.LogError("[BlockConfig] Block: " + namespacedName + " does not exist in UR or BlockTypes.properties. Id: " + blockId);
                }
                else
                {
                    // Block exists in URV but not in properties, use the URV
                    ElementKeyMap.properties.Add(namespacedName, resourceId.ToString());

                    // Need to write our properties file.
                    ElementKeyMap.writePropertiesOrdered();
                    Debug.LogError("[BlockConfig] Block: " + namespacedName + " existed in UR but not properties. Id: " + resourceId);
                }
            }
            short blockId2 = (short)ElementKeyMap.insertIntoProperties(namespacedName);
            return blockId2;
        }

        public static ElementInformation CopyOfElement(StarMod mod, ElementInformation info, string newName)
        {
            string namespacedName = mod.getName() + "~" + newName;
            short id = GetIdOfName(namespacedName);
            return new ElementInformation(info, id, newName);
        }

        //Make sure that mods call .add after .newElement
        public static List<string> AddedElementsDebug = new List<string>();

        public static ElementInformation NewElement(StarMod mod, string name, params short[] ids)
        {
            string namespacedName = mod.getName() + "~" + name;
            AddedElementsDebug.Add(namespacedName);
            short id = GetIdOfName(namespacedName);
            ElementInformation elementInformation = new ElementInformation(id, name, ElementKeyMap.getCategoryHirarchy(), ids);
            elementInformation.mod = mod;
            elementInformation.fullName = namespacedName;
            int idLength = ids.Length;
            if (idLength == 3 || idLength == 6 || idLength == 1)
            {
                elementInformation.individualSides = idLength;
                if (idLength == 1)
                {
                    short t = ids[0];
                    ids = new short[] { t, t, t, t, t, t };
                    elementInformation.setTextureId(ids);
                }
                else if (idLength == 3)
                {
                    ids = new short[] { ids[0], ids[1], ids[0], ids[1], ids[0], ids[1] };
                    elementInformation.setTextureId(ids);
                }
                else
                {
                    elementInformation.setTextureId(ids);
                }
            }
            else
            {
                throw new ArgumentException("You passed a texture array of: " + idLength + " to newElement, you must use sizes of 1, 3, or 6. ");
            }
            //todo resort at end
            ElementKeyMap.sortedByName.Add(elementInformation);
            return elementInformation;
        }
        public static ElementInformation NewChamber(StarMod mod, string name, short rootChamber, params StatusEffectType[] appliedEffects)
        {
            ElementInformation info = NewElement(mod, name, new short[] { 640 });
            info.blockResourceType = 2;
            info.sourceReference = 1085;
            info.chamberRoot = rootChamber;
            info.chamberParent = 1085;
            info.chamberPermission = 1;
            info.chamberPrerequisites.Add(1085);
            info.placable = false;
            info.canActivate = true;
            info.systemBlock = true;

            info.price = 100;
            info.description = "A Custom chamber";
            info.shoppable = false;
            info.mass = 0.15F;

            foreach (StatusEffectType effectType in appliedEffects) info.chamberConfigGroupsLowerCase.Add(effectType.name().ToLower());
            //info.chamberConfigGroupsLowerCase.add(appliedEffect.name().toLowerCase(Locale.ENGLISH));
            ElementKeyMap.chamberAnyTypes.Add(info.id);

            ElementInformation parentInfo = ElementKeyMap.getInfo(rootChamber);
            parentInfo.chamberChildren.Add(info.id);
            return info;
        }

        private static List<ElementInformation> elements = new List<ElementInformation>();
        public static List<ElementInformation> GetElements()
        {
            return elements;
        }
        public static void Add(ElementInformation entry)
        {
            //Add the elements to this list that doesnt really do anything
            elements.Add(entry);
            AddedElementsDebug.Remove(entry.fullName);
            try
            {
                //Recreate the texture mappings so the game knows what textures to use
                MethodInfo m = typeof(ElementInformation).GetMethod("recreateTextureMapping", BindingFlags.NonPublic | BindingFlags.Instance);
                m.Invoke(entry, null);
                //Add it
                ElementKeyMap.addInformationToExisting(entry);
            }
            catch (Exception e)
            {
                Debug.LogException(e);
            }
        }
        public static void PrintElementDebug()
        {
            if (AddedElementsDebug.Count > 0)
            {
                Debug.LogError("[WARNING] Some elements have been registered through newElement, but not added by .add!");
                foreach (string s in AddedElementsDebug)
                {
                    Debug.LogError("Element: " + s);
                }
                Debug.LogError("==========================================");
            }
        }
        public static void MakeFakeBlocksForUnloadedMods()
        {
            Debug.LogError("[BlockConfig] Making fake blocks for unloaded mods... (if any)");
            foreach (KeyValuePair<object, object> entry in ElementKeyMap.properties)
            {
                int id = int.Parse(entry.Value.ToString());
                ElementInformation info = null;
                if (id < ElementKeyMap.infoArray.Length)
                {
                    info = ElementKeyMap.infoArray[id];
                }
                // If the element does not exist, or is out of bounds, make a fake one
                if (info == null)
                {
                    string name = entry.Key.ToString();
                    if (name.StartsWith("WHITE_LIGHT_") || name.StartsWith("CUSTOM_ID_"))
                    {
                        // Skip WHITE_LIGHT* and CUSTOM_ID*, these are vanilla blocks without actual implementations
                        continue;
                    }
                    Debug.LogError("[BlockConfig] Block: " + entry.Key + ", id=" + entry.Value + " was in BlockTypes.properties but not in-game! Making a generic block for it");
                    short[] textureIds = new short[] { 2, 2, 2, 2, 2, 2 };
                    string namespacedName = entry.Key.ToString();
                    AddedElementsDebug.Add(namespacedName);
                    ElementInformation elementInformation = new ElementInformation((short)id,
                            "Unloaded Block: " + namespacedName,
                            ElementKeyMap.getCategoryHirarchy(), textureIds);
                    elementInformation.mod = ModPlayground.inst;
                    elementInformation.fullName = namespacedName;
                    elementInformation.setTextureId(textureIds);
                    ElementKeyMap.sortedByName.Add(elementInformation);
                    elementInformation.shoppable = false;
                    BlockConfig.SetBasicInfo(elementInformation, "generic description", 1, 0, true, false, 535);
                    Add(elementInformation);
                }
            }
        }
        public static void SetRestrictedBlock(ElementInformation elem, bool restricted)
        {
            if (restricted)
            {
                RestrictedBlocks.Add(elem.id);
            }
            else
            {
                RestrictedBlocks.Remove(elem.id);
            }
        }
    }
}