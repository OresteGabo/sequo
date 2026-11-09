import AppKit
import Foundation
let root = CommandLine.arguments[1]
let shops = try JSONSerialization.jsonObject(with: Data(contentsOf: URL(fileURLWithPath: root + "/catalog.json"))) as! [[String:Any]]
func color(_ hex:String) -> NSColor { let n=UInt32(hex.dropFirst(),radix:16)!; return NSColor(srgbRed:CGFloat((n>>16)&255)/255,green:CGFloat((n>>8)&255)/255,blue:CGFloat(n&255)/255,alpha:1) }
func text(_ s:String,_ rect:NSRect,_ size:CGFloat,_ c:NSColor,_ weight:NSFont.Weight = .semibold) {
 let p=NSMutableParagraphStyle(); p.alignment = .left; p.lineBreakMode = .byWordWrapping
 (s as NSString).draw(in:rect,withAttributes:[.font:NSFont.systemFont(ofSize:size,weight:weight),.foregroundColor:c,.paragraphStyle:p])
}
for shop in shops {
 for product in shop["products"] as! [[String:Any]] {
  let img=NSImage(size:NSSize(width:720,height:720)); img.lockFocus()
  let accent=color(shop["color"] as! String)
  NSColor(calibratedWhite:0.96,alpha:1).setFill(); NSRect(x:0,y:0,width:720,height:720).fill()
  accent.setFill(); NSBezierPath(roundedRect:NSRect(x:28,y:28,width:664,height:664),xRadius:44,yRadius:44).fill()
  NSColor.white.withAlphaComponent(0.12).setFill(); NSBezierPath(ovalIn:NSRect(x:460,y:390,width:330,height:330)).fill()
  text("SEQUO / DEMO",NSRect(x:70,y:594,width:580,height:40),24,.white)
  text(product["name"] as! String,NSRect(x:70,y:240,width:565,height:240),62,.white,.bold)
  text(shop["name"] as! String,NSRect(x:70,y:170,width:570,height:50),28,.white)
  text("TEST • " + (product["code"] as! String),NSRect(x:70,y:83,width:570,height:40),25,.white.withAlphaComponent(0.86))
  img.unlockFocus()
  let rep=NSBitmapImageRep(data:img.tiffRepresentation!)!
  try rep.representation(using:.png,properties:[:])!.write(to:URL(fileURLWithPath:root+"/"+(product["id"] as! String)+".png"))
 }
}
print("Rendered 120 labelled PNG product cards.")
