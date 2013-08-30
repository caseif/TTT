package net.amigocraft.TTT.utils;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryUtils {

	public static void removeArrow(Inventory inv){
		for (int i = 0; i < inv.getContents().length; i++){
			ItemStack is = inv.getItem(i);
			if (is != null){
				if (is.getType() == Material.ARROW){
					if (is.getAmount() == 1)
						inv.setItem(i, null);
					else if (is.getAmount() > 1)
						is.setAmount(is.getAmount() - 1);
					break;
				}
			}
		}
	}
	
}
