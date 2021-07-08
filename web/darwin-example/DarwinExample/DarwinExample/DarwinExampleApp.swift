//
//  DarwinExampleApp.swift
//  DarwinExample
//
//  Created by Tadeas Kriz on 16.06.2021.
//

import SwiftUI
import darwin_example

@main
struct DarwinExampleApp: App {
    var body: some Scene {
        WindowGroup {
            ZStack {
                ComposableView()
            }
        }
    }
}

class RootView: UIView {
}

struct ComposableView: UIViewRepresentable {
    func makeUIView(context: Context) -> some UIView {
        return IosAppKt.IosApp.toView()
    }

    func updateUIView(_ uiView: UIViewType, context: Context) {
//        IosAppKt.IosApp.attachMain(view: uiView)
    }
}

//
//import UIKit
//
//class X: UIView {
//
//    override func didMoveToWindow() {
//        <#code#>
//    }
//
//    override func willMove(toWindow newWindow: UIWindow?) {
//        <#code#>
//    }
//
//}
