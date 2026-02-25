//
// Created by Lucas Yuji Yoshimine on 31/08/2025.
//

import Foundation
import UIKit
import SwiftUI
import sharedKit

struct CameraView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        CameraViewController_iosKt.CameraViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}