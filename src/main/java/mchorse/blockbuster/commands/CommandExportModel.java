package mchorse.blockbuster.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import mchorse.blockbuster.ClientProxy;
import mchorse.blockbuster.client.model.parsing.ModelExporter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.command.server.CommandSummon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

/**
 * Command /export-model
 *
 * This command is responsible for converting (i.e. exporting) in-game Minecraft
 * models (ModelBase or his children) to JSON scheme that supports my custom
 * models.
 *
 * This is attempt number two.
 */
public class CommandExportModel extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "export-model";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "blockbuster.commands.export_model";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 1)
        {
            throw new WrongUsageException(this.getCommandUsage(sender));
        }

        String type = args[0];
        Entity entity = EntityList.createEntityByName(type, sender.getEntityWorld());
        Render render = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(entity);

        if (render == null || !(render instanceof RenderLivingBase) || !(entity instanceof EntityLivingBase))
        {
            throw new CommandException("Can't export model for \"%s\" type (because it has no renderer, or it's not a living entity renderer).", type);
        }

        ModelExporter exporter = new ModelExporter((EntityLivingBase) entity, (RenderLivingBase) render);

        String output = exporter.export(type);
        File exportFolder = new File(ClientProxy.config.getAbsolutePath() + "/export");

        exportFolder.mkdirs();

        try
        {
            PrintWriter writer = new PrintWriter(new File(ClientProxy.config.getAbsolutePath() + "/export/" + type + ".json"));

            writer.print(output);
            writer.close();

            sender.addChatMessage(new TextComponentString("Model " + type + " was saved to export/" + type + ".json"));
        }
        catch (FileNotFoundException e)
        {
            throw new CommandException("Exported model couldn't be saved.");
        }
    }

    /**
     * Auto-complete entity type list
     *
     * Brutally ripped from {@link CommandSummon} class
     */
    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, EntityList.getEntityNameList()) : Collections.<String> emptyList();
    }
}