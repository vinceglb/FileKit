//
//  ContentView.swift
//  iOSApp
//
//  Created by Vincent Guillebaud on 04/04/2024.
//

import SwiftUI
import SamplePickerKt

struct ContentView: View {
    @State var viewModel = MainViewModel(dialogSettings: Filekit_dialogsFileKitDialogSettings(canCreateDirectories: true))
    @State var uiState: MainUiState = .init()
    
    var body: some View {
        // Convert Set to Array
        let files = Array(uiState.files)

        VStack {
            Image(systemName: "globe")
                .imageScale(.large)
                .foregroundStyle(.tint)
            Text("Hello, world!")

            Button("Single image picker") {
                viewModel.pickImage()
            }

            Button("Multiple images picker") {
                viewModel.pickImages()
            }

            Button("Single file picker, only png") {
                viewModel.pickFile()
            }

            Button("Multiple file picker, only png") {
                viewModel.pickFiles()
            }
            
            Button("Multiple file picker with state") {
                viewModel.pickFilesWithState()
            }

            Button("Directory picker") {
                viewModel.pickDirectory()
            }

            if uiState.loading {
                ProgressView()
            }

            Text("Directory: \(String(describing:  uiState.directory?.path))")
            
            NavigationView {
                List(files, id: \.nsUrl) { file in
                    NavigationLink(file.name) {
                        Form {
                            Button {
                                viewModel.saveFile(file: file)
                            } label: {
                                Label("Save",systemImage: "square.and.arrow.down")
                            }
                            Button {
                                viewModel.shareFile(file: file)
                            } label: {
                                Label("Share",systemImage: "square.and.arrow.up")
                            }
                        }
                        .navigationTitle(file.name)
                    }
                }
            }
        }
        .padding()
        .task {
            viewModel.uiState.collect(collector: Collector<MainUiState> { state in
                self.uiState = state
            }) { (error) in }
        }
    }
}

#Preview {
    ContentView()
}
