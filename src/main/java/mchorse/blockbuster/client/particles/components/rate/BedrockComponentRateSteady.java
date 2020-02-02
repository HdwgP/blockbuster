package mchorse.blockbuster.client.particles.components.rate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mchorse.blockbuster.client.particles.components.BedrockComponentBase;
import mchorse.blockbuster.client.particles.molang.MolangException;
import mchorse.blockbuster.client.particles.molang.MolangParser;

public class BedrockComponentRateSteady extends BedrockComponentBase
{
	public float spawnRate;
	public int maxParticles;

	public BedrockComponentBase fromJson(JsonElement elem, MolangParser parser) throws MolangException
	{
		if (!elem.isJsonObject()) return super.fromJson(elem, parser);

		JsonObject element = elem.getAsJsonObject();

		if (element.has("spawn_rate")) this.spawnRate = element.get("spawn_rate").getAsFloat();
		if (element.has("max_particles")) this.maxParticles = element.get("max_particles").getAsInt();

		return super.fromJson(element, parser);
	}
}