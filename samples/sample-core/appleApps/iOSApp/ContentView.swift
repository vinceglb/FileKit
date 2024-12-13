import SwiftUI
import KMPObservableViewModelSwiftUI
import SamplePickerKt

struct ContentView: View {
    @StateViewModel
    var viewModel = MainViewModel(platformSettings: nil)
    
    @State private var sheetIsPresented: Bool = false
    
    var body: some View {
        let uiState = viewModel.uiState.value as? MainUiState
        
        // Convert Set to Array
        let files = Array(uiState?.files ?? [])
        
        VStack {
            Button("ShowSheet") {
                sheetIsPresented.toggle()
            }
            .sheet(isPresented: $sheetIsPresented, content: {
                Button("Multiple file picker, only png") {
                    viewModel.pickFiles()
                }
            })
        
            if uiState?.loading == true {
                ProgressView()
            }
            
            Text("Directory: \(String(describing: uiState?.directory?.path))")
            
            List(files, id: \.nsUrl) { file in
                Text(file.name)
                    .onTapGesture { viewModel.saveFile(file: file) }
            }
        }
        .padding()
    }
}
