import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BristolInputComponent } from './bristol-input.component';

describe('BristolInputComponent', () => {
  let component: BristolInputComponent;
  let fixture: ComponentFixture<BristolInputComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ BristolInputComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BristolInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
