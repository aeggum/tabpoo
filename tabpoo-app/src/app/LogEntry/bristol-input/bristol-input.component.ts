import { Component, OnInit, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-bristol-input',
  templateUrl: './bristol-input.component.html',
  styleUrls: ['./bristol-input.component.css']
})
export class BristolInputComponent implements OnInit {

  selection: number = null;
  options: number[] = [1,2,3,4,5,6,7];
  @Output() select = new EventEmitter<number>();

  constructor() { }

  ngOnInit() {
  }

  onSelection(selection: number) {
    this.selection = selection;
    this.select.emit(this.selection);
  }

}
