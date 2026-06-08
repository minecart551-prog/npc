package noppes.npcs;

public class ScriptItemEventHandler {


//	@SubscribeEvent
//	public void invoke(ItemTossEvent event) {
//		if(event.getPlayer().level().isClientSide)
//			return;
//
//		ItemEntity entity = event.getEntity();
//		ItemStack stack = entity.getItem();
//		if(!stack.isEmpty() && stack.getItem() == CustomItems.scripted_item) {
//			if(EventHooks.onScriptItemTossed(ItemScripted.GetWrapper(stack), event.getPlayer(), entity)) {
//				event.setCanceled(true);
//			}
//		}
//	}
//
//	@SubscribeEvent
//	public void invoke(EntityItemPickupEvent event) {
//		if(event.getEntity().level().isClientSide)
//			return;
//		ItemEntity entity = event.getItem();
//		ItemStack stack = entity.getItem();
//		if(!stack.isEmpty() && stack.getItem() == CustomItems.scripted_item) {
//			EventHooks.onScriptItemPickedUp(ItemScripted.GetWrapper(stack), event.getEntity(), entity);
//		}
//	}
}
