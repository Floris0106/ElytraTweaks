package floris0106.elytratweaks;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;

import org.apache.logging.log4j.LogManager;
import org.lwjgl.glfw.GLFW;

public class ElytraTweaks implements ClientModInitializer
{
	public static final KeyMapping STOP_FLYING_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.elytratweaks.stop_flying", GLFW.GLFW_KEY_V, "key.categories.movement"));

	@Override
	public void onInitializeClient()
	{
		LogManager.getLogger().info("Initialized ElytraTweaks!");
	}
}