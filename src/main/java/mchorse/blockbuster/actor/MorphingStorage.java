package mchorse.blockbuster.actor;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

/**
 * Morphing storage
 *
 * This class is responsible for saving IMorphing capability to... Hey, Houston,
 * where these data are getting saved? Basically, I don't know.
 */
public class MorphingStorage implements IStorage<IMorphing>
{
    @Override
    public NBTBase writeNBT(Capability<IMorphing> capability, IMorphing instance, EnumFacing side)
    {
        NBTTagCompound tag = new NBTTagCompound();

        tag.setString("Model", instance.getModel());
        tag.setString("Skin", instance.getSkin());

        System.out.println(instance.getModel() + ", " + instance.getSkin());

        return tag;
    }

    @Override
    public void readNBT(Capability<IMorphing> capability, IMorphing instance, EnumFacing side, NBTBase nbt)
    {
        if (nbt instanceof NBTTagCompound)
        {
            NBTTagCompound tag = (NBTTagCompound) nbt;

            instance.setModel(tag.getString("Model"));
            instance.setSkin(tag.getString("Skin"));

            System.out.println(instance.getModel() + ", " + instance.getSkin());
        }
    }
}
