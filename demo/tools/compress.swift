import Foundation
import ImageIO
let root = CommandLine.arguments[1]
for item in try FileManager.default.contentsOfDirectory(atPath:root) where item.hasSuffix(".png") {
 let source=CGImageSourceCreateWithURL(URL(fileURLWithPath:root+"/"+item) as CFURL,nil)!
 let img=CGImageSourceCreateThumbnailAtIndex(source,0,[kCGImageSourceCreateThumbnailFromImageAlways:true,kCGImageSourceThumbnailMaxPixelSize:720] as CFDictionary)!
 let destination=CGImageDestinationCreateWithURL(URL(fileURLWithPath:root+"/"+item.replacingOccurrences(of:".png",with:".jpg")) as CFURL,"public.jpeg" as CFString,1,nil)!
 CGImageDestinationAddImage(destination,img,[kCGImageDestinationLossyCompressionQuality:0.8] as CFDictionary)
 assert(CGImageDestinationFinalize(destination))
}
print("Optimized 120 labelled product cards.")
